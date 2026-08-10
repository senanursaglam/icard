package com.example.icardbackend.user.service;

import com.example.icardbackend.common.exception.DepartmentException.DepartmentNotFoundException;
import com.example.icardbackend.department.entity.Department;
import com.example.icardbackend.department.repository.DepartmentRepository;
import com.example.icardbackend.security.jwt.JwtService;
import com.example.icardbackend.security.userdetails.CustomUserDetails;
import com.example.icardbackend.user.dto.*;
import com.example.icardbackend.user.entity.RoleType;
import com.example.icardbackend.user.entity.User;
import com.example.icardbackend.user.mapper.UserMapper;
import com.example.icardbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.icardbackend.common.exception.UserException.EmailAlreadyExistsException;
import com.example.icardbackend.common.exception.UserException.UserNotFoundException;

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


    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) { //dbden bu maile ait kullanıcı var mı diye bakıyor repo aracılığıyla
            throw new EmailAlreadyExistsException("Bu email adresi zaten kayıtlı: " + request.getEmail());
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = userMapper.toEntity(request, encodedPassword); //builder
        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser); //direkt entity'yi dönmek yerine UserResponse döndük
    }

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
                .build();
    }

    public UserResponse adminCreateUser(AdminUserCreateRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Bu email adresi zaten kayıtlı: " + request.getEmail());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : RoleType.EMPLOYEE)
                .department(resolveDepartment(request.getDepartmentId()))
                .manager(resolveManager(request.getManagerId()))
                .active(true)
                .build();

        return userMapper.toResponse(userRepository.save(user));
    }

    public UserResponse adminUpdateUser(Long id, AdminUserUpdateRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + id));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getDepartmentId() != null) user.setDepartment(resolveDepartment(request.getDepartmentId()));
        if (request.getManagerId() != null) user.setManager(resolveManager(request.getManagerId()));

        return userMapper.toResponse(userRepository.save(user));
    }

    public void adminDeactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + id));
        user.setActive(false);
        userRepository.save(user);
    }

    public List<UserResponse> adminGetAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
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
}