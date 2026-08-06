package com.example.icardbackend.attendance.service;

import com.example.icardbackend.attendance.dto.AttendanceFilterRequest;
import com.example.icardbackend.attendance.entity.Attendance;
import com.example.icardbackend.attendance.entity.AttendanceStatus;
import com.example.icardbackend.attendance.repository.AttendanceRepository;
import com.example.icardbackend.attendance.specification.AttendanceSpecificationBuilder;
import com.example.icardbackend.common.exception.AttendanceException.ActiveAttendanceExistsException;
import com.example.icardbackend.common.exception.AttendanceException.NoActiveAttendanceException;
import com.example.icardbackend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceSpecificationBuilder specificationBuilder;


    // QR doğrulandıktan sonra çağrılacak, attendance kaydı oluşturur
    public Attendance checkIn(User user) {

        attendanceRepository.findByUserAndLogoutTimeIsNull(user)
                .ifPresent(existing -> {
                    throw new ActiveAttendanceExistsException(
                            "Zaten aktif bir girişiniz var. Önce çıkış yapmalısınız."
                    );
                });

        Attendance attendance = Attendance.builder()
                .user(user)
                .loginTime(LocalDateTime.now())
                .status(AttendanceStatus.ACTIVE)
                .build();

        return attendanceRepository.save(attendance);
    }

    // "Çıkış Yap" butonuna basınca çağrılacak
    public Attendance checkOut(User user) {

        Attendance attendance = attendanceRepository.findByUserAndLogoutTimeIsNull(user)
                .orElseThrow(() -> new NoActiveAttendanceException(
                        "Aktif bir girişiniz bulunmuyor."
                ));

        LocalDateTime now = LocalDateTime.now();
        long minutes = Duration.between(attendance.getLoginTime(), now).toMinutes();

        attendance.setLogoutTime(now);
        attendance.setWorkedMinutes(minutes);
        attendance.setWorkedHours(minutes / 60.0);
        attendance.setStatus(AttendanceStatus.COMPLETED);

        return attendanceRepository.save(attendance);
    }

    public Page<Attendance> getAttendanceHistory(User user, Pageable pageable) {
        return attendanceRepository.findByUser(user, pageable);
    }

    public Page<Attendance> filterAttendance(User user, AttendanceFilterRequest request) {

        Specification<Attendance> spec = specificationBuilder.build(user, request.getFilters());

        Pageable pageable = buildPageable(request);

        return attendanceRepository.findAll(spec, pageable);
    }

    private Pageable buildPageable(AttendanceFilterRequest request) {
        if (request.getSort() != null && !request.getSort().isBlank()) {
            String[] sortParts = request.getSort().split(",");
            Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            return PageRequest.of(request.getPage(), request.getSize(), Sort.by(direction, sortParts[0]));
        }
        return PageRequest.of(request.getPage(), request.getSize());
    }

    public boolean hasActiveAttendance(User user) {
     return attendanceRepository.findByUserAndLogoutTimeIsNull(user).isPresent();
    }
}