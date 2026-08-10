package com.example.icardbackend.department.service;

import com.example.icardbackend.attendance.repository.AttendanceRepository;
import com.example.icardbackend.common.exception.DepartmentException.DepartmentAlreadyExistsException;
import com.example.icardbackend.common.exception.DepartmentException.DepartmentNotFoundException;
import com.example.icardbackend.common.exception.UserException.UserNotFoundException;
import com.example.icardbackend.department.dto.DepartmentRequest;
import com.example.icardbackend.department.dto.DepartmentStatsResponse;
import com.example.icardbackend.department.entity.Department;
import com.example.icardbackend.department.repository.DepartmentRepository;
import com.example.icardbackend.user.entity.RoleType;
import com.example.icardbackend.user.entity.User;
import com.example.icardbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;

    public Department create(DepartmentRequest request) {

        if (departmentRepository.existsByName(request.getName())) {
            throw new DepartmentAlreadyExistsException("Bu departman adı zaten kullanılıyor: " + request.getName());
        }

        Department department = Department.builder()
                .name(request.getName())
                .manager(resolveManager(request.getManagerId()))
                .build();

        return departmentRepository.save(department);
    }

    public Department update(Long id, DepartmentRequest request) {

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException("Departman bulunamadı: " + id));

        department.setName(request.getName());
        department.setManager(resolveManager(request.getManagerId()));

        return departmentRepository.save(department);
    }

    public java.util.List<Department> getAll() {
        return departmentRepository.findAll();
    }

    private User resolveManager(Long managerId) {
        if (managerId == null) return null;
        return userRepository.findById(managerId)
                .orElseThrow(() -> new UserNotFoundException("Yönetici bulunamadı: " + managerId));
    }

    public DepartmentStatsResponse getStats(Long departmentId, User requester) {

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException("Departman bulunamadı: " + departmentId));

        if (requester.getRole() == RoleType.MANAGER) {
            boolean isOwnDepartment = requester.getDepartment() != null
                    && requester.getDepartment().getId().equals(departmentId);

            if (!isOwnDepartment) {
                throw new AccessDeniedException("Sadece kendi departmanınızın istatistiklerine erişebilirsiniz.");
            }
        }

        List<User> members = userRepository.findByDepartment(department);

        if (members.isEmpty()) {
            return DepartmentStatsResponse.builder()
                    .departmentId(department.getId())
                    .departmentName(department.getName())
                    .memberCount(0)
                    .weeklyWorkedHours(0.0)
                    .totalWorkedHours(0.0)
                    .build();
        }

        LocalDateTime weekStart = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();
        LocalDateTime weekEnd = weekStart.plusWeeks(1);

        Long weeklyMinutes = attendanceRepository.sumWorkedMinutesForUsersBetween(members, weekStart, weekEnd);
        Long totalMinutes = attendanceRepository.sumAllWorkedMinutesForUsers(members);

        return DepartmentStatsResponse.builder()
                .departmentId(department.getId())
                .departmentName(department.getName())
                .memberCount(members.size())
                .weeklyWorkedHours(minutesToHours(weeklyMinutes))
                .totalWorkedHours(minutesToHours(totalMinutes))
                .build();
    }

    private Double minutesToHours(Long minutes) {
        if (minutes == null) return 0.0;
        double hours = minutes / 60.0;
        return Math.round(hours * 100.0) / 100.0;
    }
}