package com.example.icardbackend.user.service;

import com.example.icardbackend.attendance.entity.Attendance;
import com.example.icardbackend.attendance.mapper.AttendanceMapper;
import com.example.icardbackend.attendance.repository.AttendanceRepository;
import com.example.icardbackend.common.exception.DepartmentException.DepartmentNotFoundException;
import com.example.icardbackend.common.specification.FilterCriteria;
import com.example.icardbackend.department.entity.Department;
import com.example.icardbackend.department.repository.DepartmentRepository;
import com.example.icardbackend.leave.entity.LeaveStatus;
import com.example.icardbackend.leave.mapper.LeaveRequestMapper;
import com.example.icardbackend.leave.repository.LeaveRequestRepository;
import com.example.icardbackend.security.jwt.JwtService;
import com.example.icardbackend.security.userdetails.CustomUserDetails;
import com.example.icardbackend.user.dto.*;
import com.example.icardbackend.user.entity.RoleType;
import com.example.icardbackend.user.entity.User;
import com.example.icardbackend.user.mapper.UserMapper;
import com.example.icardbackend.user.repository.UserRepository;
import com.example.icardbackend.user.specification.UserSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.icardbackend.common.exception.UserException.EmailAlreadyExistsException;
import com.example.icardbackend.common.exception.UserException.UserNotFoundException;
import com.example.icardbackend.qr.service.MailService;
import java.util.UUID;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final DepartmentRepository departmentRepository;
    private final UserSpecificationBuilder userSpecificationBuilder;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;
    private final LeaveRequestRepository leaveRequestRepository;
    private final MailService mailService;

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        // yanlış şifre/mail girilirse buradan exception fırlatılır
        // GlobalExceptionHandler bunu otomatik yakalayıp 401 dönecek

        User user = userRepository.findByEmail(request.getEmail()) //dbden bu maile ait kullanıcıyı buluyor bakıyor repo aracılığıyla
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + request.getEmail()));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .user(userMapper.toResponse(user))
                .temporaryPassword(user.getTemporaryPassword())
                .build();
    }

    public UserResponse adminCreateUser(AdminUserCreateRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Bu email adresi zaten kayıtlı: " + request.getEmail()
            );
        }

        Department department = resolveDepartment(request.getDepartmentId());

        String temporaryPassword = generateTemporaryPassword();

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(temporaryPassword))
                .role(request.getRole() != null ? request.getRole() : RoleType.EMPLOYEE)
                .department(department)
                .manager(department != null ? department.getManager() : null)
                .active(true)
                .temporaryPassword(true)
                .build();

        User savedUser = userRepository.save(user);

        mailService.sendTemporaryPasswordMail(
                savedUser.getEmail(),
                savedUser.getFirstName(),
                temporaryPassword
        );

        return userMapper.toResponse(savedUser);
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10);
    }

    public UserResponse adminUpdateUser(Long id, AdminUserUpdateRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + id));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getRole() != null) user.setRole(request.getRole());

        if (request.getDepartmentId() != null) {
            Department department = resolveDepartment(request.getDepartmentId());
            user.setDepartment(department);
            user.setManager(department != null ? department.getManager() : null);
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    public void adminDeactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + id));
        user.setActive(false);
        userRepository.save(user);
    }
    public Page<UserResponse> adminGetAllUsers(int page, int size, List<FilterCriteria> filters) {
        Specification<User> spec = userSpecificationBuilder.build(filters);
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(spec, pageable).map(userMapper::toResponse);
    }

    private Department resolveDepartment(Long departmentId) {
        if (departmentId == null) return null;
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException("Departman bulunamadı: " + departmentId));
    }

    private User resolveManager(Long managerId) {
        if (managerId == null) return null;
        return userRepository.findById(managerId)
                .orElseThrow(() -> new UserNotFoundException("Yönetici bulunamadı: " + managerId));
    }

    public List<UserResponse> getTeam(User manager) {
        return userRepository.findByManager(manager).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public List<User> getTeamEntities(User manager) {
        return userRepository.findByManager(manager);
    }

    public EmployeeDetailResponse getEmployeeDetail(Long employeeId, User requester) {

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + employeeId));

        boolean isOwnTeamMember = employee.getManager() != null
                && employee.getManager().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == RoleType.ADMIN;

        if (!isOwnTeamMember && !isAdmin) {
            throw new AccessDeniedException("Sadece kendi ekibinizdeki personelin detaylarını görüntüleyebilirsiniz.");
        }

        List<Attendance> recentAttendance = attendanceRepository
                .findByUserOrderByLoginTimeDesc(employee, PageRequest.of(0, 10));

        Long overtimeMinutes = attendanceRepository.sumAllOvertimeMinutesForUser(employee);
        long lateCount = attendanceRepository.countAllLateForUser(employee);
        long pendingLeaves = leaveRequestRepository.findByUser(employee).stream()
                .filter(lr -> lr.getStatus() == LeaveStatus.PENDING)
                .count();

        return EmployeeDetailResponse.builder()
                .user(userMapper.toResponse(employee))
                .recentAttendance(recentAttendance.stream().map(attendanceMapper::toAttendanceResponse).toList())
                .totalOvertimeHours(overtimeMinutes != null ? Math.round((overtimeMinutes / 60.0) * 100.0) / 100.0 : 0.0)
                .lateCount(lateCount)
                .pendingLeaveCount((int) pendingLeaves)
                .build();
    }

    public void changePassword(
            String email,
            ChangePasswordRequest request
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException("Kullanıcı bulunamadı.")
                );

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setTemporaryPassword(false);

        userRepository.save(user);
    }
}