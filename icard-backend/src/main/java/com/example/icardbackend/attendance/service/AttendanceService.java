package com.example.icardbackend.attendance.service;

import com.example.icardbackend.attendance.dto.AttendanceFilterRequest;
import com.example.icardbackend.attendance.dto.TeamOvertimeSummaryResponse;
import com.example.icardbackend.attendance.entity.Attendance;
import com.example.icardbackend.attendance.entity.AttendanceStatus;
import com.example.icardbackend.attendance.repository.AttendanceRepository;
import com.example.icardbackend.attendance.specification.AttendanceSpecificationBuilder;
import com.example.icardbackend.common.exception.AttendanceException.ActiveAttendanceExistsException;
import com.example.icardbackend.common.exception.AttendanceException.NoActiveAttendanceException;
import com.example.icardbackend.user.entity.User;
import com.example.icardbackend.workschedule.entity.WorkSchedule;
import com.example.icardbackend.workschedule.repository.WorkScheduleRepository;
import com.example.icardbackend.workschedule.service.WorkScheduleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceSpecificationBuilder specificationBuilder;
    private final WorkScheduleService workScheduleService;

    // QR doğrulandıktan sonra çağrılacak, attendance kaydı oluşturur
    @Transactional
    public Attendance checkIn(User user) {

        attendanceRepository.findByUserAndLogoutTimeIsNull(user)
                .ifPresent(existing -> {
                    throw new ActiveAttendanceExistsException(
                            "Zaten aktif bir girişiniz var. Önce çıkış yapmalısınız."
                    );
                });

        LocalDateTime now = LocalDateTime.now();
        boolean late = false;

        WorkSchedule schedule = workScheduleService.resolveForUser(user);
        if (schedule != null) {
            LocalTime latestAllowed = schedule.getStartTime().plusMinutes(schedule.getToleranceMinutes());
            late = now.toLocalTime().isAfter(latestAllowed);
        }

        Attendance attendance = Attendance.builder()
                .user(user)
                .loginTime(now)
                .status(AttendanceStatus.ACTIVE)
                .isLate(late)
                .build();

        return attendanceRepository.save(attendance);
    }

    // "Çıkış Yap" butonuna basınca çağrılacak
    @Transactional
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

        WorkSchedule schedule = workScheduleService.resolveForUser(user);
        if (schedule != null) {
            LocalTime logoutTime = now.toLocalTime();

            if (logoutTime.isAfter(schedule.getEndTime())) {
                long overtime = Duration.between(schedule.getEndTime(), logoutTime).toMinutes();
                attendance.setOvertimeMinutes(overtime);
                attendance.setIsEarlyLeave(false);
            } else if (logoutTime.isBefore(schedule.getEndTime())) {
                attendance.setOvertimeMinutes(0L);
                attendance.setIsEarlyLeave(true);
            } else {
                attendance.setOvertimeMinutes(0L);
                attendance.setIsEarlyLeave(false);
            }
        }

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

    public Page<Attendance> filterTeamAttendance(List<User> team, AttendanceFilterRequest request) {

        if (team.isEmpty()) {
            return Page.empty();
        }

        Specification<Attendance> spec = specificationBuilder.buildForTeam(team, request.getFilters());
        Pageable pageable = buildPageable(request);

        return attendanceRepository.findAll(spec, pageable);
    }

    public List<TeamOvertimeSummaryResponse> getTeamOvertimeSummary(List<User> team) {

        List<TeamOvertimeSummaryResponse> result = new ArrayList<>();

        for (User member : team) {
            Long overtimeMinutes = attendanceRepository.sumAllOvertimeMinutesForUser(member);
            long lateCount = attendanceRepository.countAllLateForUser(member);

            result.add(TeamOvertimeSummaryResponse.builder()
                    .userFullName(member.getFirstName() + " " + member.getLastName())
                    .totalOvertimeHours(minutesToHours(overtimeMinutes))
                    .lateCount(lateCount)
                    .build());
        }

        return result;
    }

    private double minutesToHours(Long minutes) {
        if (minutes == null) return 0.0;
        double hours = minutes / 60.0;
        return Math.round(hours * 100.0) / 100.0;
    }

    public Page<Attendance> filterAllAttendance(AttendanceFilterRequest request) {
        Specification<Attendance> spec = specificationBuilder.buildForAdmin(request.getFilters());
        Pageable pageable = buildPageable(request);
        return attendanceRepository.findAll(spec, pageable);
    }
}