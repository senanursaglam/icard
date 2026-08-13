package com.example.icardbackend.attendance.service;

import com.example.icardbackend.attendance.dto.DailyHoursResponse;
import com.example.icardbackend.attendance.dto.DashboardStatsResponse;
import com.example.icardbackend.attendance.dto.WeeklyChartResponse;
import com.example.icardbackend.attendance.entity.Attendance;
import com.example.icardbackend.attendance.repository.AttendanceRepository;
import com.example.icardbackend.common.exception.DepartmentException.DepartmentNotFoundException;
import com.example.icardbackend.department.entity.Department;
import com.example.icardbackend.department.repository.DepartmentRepository;
import com.example.icardbackend.leave.entity.LeaveRequest;
import com.example.icardbackend.leave.repository.LeaveRequestRepository;
import com.example.icardbackend.user.entity.User;
import com.example.icardbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public DashboardStatsResponse getStats(User user) {

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);

        LocalDate weekStartDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime weekStart = weekStartDate.atStartOfDay();
        LocalDateTime weekEnd = weekStart.plusWeeks(1);

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = monthStart.plusMonths(1);

        Long todayMinutes = attendanceRepository.sumWorkedMinutesBetween(user, todayStart, todayEnd);
        Long weeklyMinutes = attendanceRepository.sumWorkedMinutesBetween(user, weekStart, weekEnd);
        Long weeklyCheckIns = attendanceRepository.countCheckInsBetween(user, weekStart, weekEnd);
        Long weeklyCheckOuts = attendanceRepository.countCheckOutsBetween(user, weekStart, weekEnd);
        Long totalMinutes = attendanceRepository.sumAllWorkedMinutes(user);

        Set<LocalDate> leaveDates = getApprovedLeaveDates(user, weekStartDate, weekStartDate.plusDays(6));

        long monthlyLateCount = attendanceRepository.countLateBetween(user, monthStart, monthEnd);
        Long monthlyOvertimeMinutes = attendanceRepository.sumOvertimeMinutesBetween(user, monthStart, monthEnd);

        return DashboardStatsResponse.builder()
                .todayWorkedHours(minutesToHours(todayMinutes))
                .weeklyWorkedHours(minutesToHours(weeklyMinutes))
                .weeklyCheckInCount(weeklyCheckIns)
                .weeklyCheckOutCount(weeklyCheckOuts)
                .totalWorkedHours(minutesToHours(totalMinutes))
                .weeklyLeaveDayCount(leaveDates.size())
                .monthlyLateCount(monthlyLateCount)
                .monthlyOvertimeHours(minutesToHours(monthlyOvertimeMinutes))
                .build();
    }

    private Double minutesToHours(Long minutes) {
        if (minutes == null) return 0.0;
        double hours = minutes / 60.0;
        return Math.round(hours * 100.0) / 100.0; //manually make it two digit after decimal
    }

    public WeeklyChartResponse getWeeklyChart(User user, int weekOffset) {

        LocalDate weekStartDate = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .plusWeeks(weekOffset);

        LocalDateTime weekStart = weekStartDate.atStartOfDay();
        LocalDateTime weekEnd = weekStart.plusWeeks(1);

        List<Attendance> weekAttendances = attendanceRepository
                .findByUserAndLoginTimeBetween(user, weekStart, weekEnd);

        Map<DayOfWeek, Long> minutesByDay = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            minutesByDay.put(day, 0L);
        }

        for (Attendance a : weekAttendances) {
            if (a.getWorkedMinutes() != null) {
                DayOfWeek day = a.getLoginTime().getDayOfWeek();
                minutesByDay.merge(day, a.getWorkedMinutes(), Long::sum);
            }
        }

        Set<LocalDate> leaveDates = getApprovedLeaveDates(user, weekStartDate, weekStartDate.plusDays(6));

        String[] labels = {"Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Pazar"};
        DayOfWeek[] order = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};

        List<DailyHoursResponse> days = new ArrayList<>();
        for (int i = 0; i < order.length; i++) {
            LocalDate currentDate = weekStartDate.plusDays(i);
            boolean isLeave = leaveDates.contains(currentDate);

            Long minutes = minutesByDay.get(order[i]);
            double hours = Math.round((minutes / 60.0) * 100.0) / 100.0;

            days.add(DailyHoursResponse.builder()
                    .day(labels[i])
                    .hours(isLeave && hours == 0 ? 8.0 : hours) // izinliyse ve hiç çalışılmamışsa sembolik 8 saat göster
                    .isLeave(isLeave)
                    .build());
        }

        return WeeklyChartResponse.builder()
                .weekStart(weekStartDate)
                .weekEnd(weekStartDate.plusDays(6))
                .days(days)
                .build();
    }

    private Set<LocalDate> getApprovedLeaveDates(User user, LocalDate weekStart, LocalDate weekEnd) {
        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findApprovedOverlapping(
                user, weekStart, weekEnd);

        Set<LocalDate> leaveDates = new HashSet<>();
        for (LeaveRequest leave : approvedLeaves) {
            LocalDate current = leave.getStartDate().isBefore(weekStart) ? weekStart : leave.getStartDate();
            LocalDate end = leave.getEndDate().isAfter(weekEnd) ? weekEnd : leave.getEndDate();

            while (!current.isAfter(end)) {
                leaveDates.add(current);
                current = current.plusDays(1);
            }
        }

        return leaveDates;
    }

    public WeeklyChartResponse getWeeklyChartForDepartment(Long departmentId, int weekOffset) {

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException("Departman bulunamadı: " + departmentId));

        List<User> members = userRepository.findByDepartment(department);

        LocalDate weekStartDate = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .plusWeeks(weekOffset);
        LocalDateTime weekStart = weekStartDate.atStartOfDay();
        LocalDateTime weekEnd = weekStart.plusWeeks(1);

        Map<DayOfWeek, Long> minutesByDay = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            minutesByDay.put(day, 0L);
        }

        if (!members.isEmpty()) {
            List<Attendance> attendances = attendanceRepository.findByUserInAndLoginTimeBetween(members, weekStart, weekEnd);
            for (Attendance a : attendances) {
                if (a.getWorkedMinutes() != null) {
                    DayOfWeek day = a.getLoginTime().getDayOfWeek();
                    minutesByDay.merge(day, a.getWorkedMinutes(), Long::sum);
                }
            }
        }

        String[] labels = {"Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Pazar"};
        DayOfWeek[] order = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};

        List<DailyHoursResponse> days = new ArrayList<>();
        for (int i = 0; i < order.length; i++) {
            Long minutes = minutesByDay.get(order[i]);
            double hours = Math.round((minutes / 60.0) * 100.0) / 100.0;
            days.add(DailyHoursResponse.builder().day(labels[i]).hours(hours).isLeave(false).build());
        }

        return WeeklyChartResponse.builder()
                .weekStart(weekStartDate)
                .weekEnd(weekStartDate.plusDays(6))
                .days(days)
                .build();
    }

}