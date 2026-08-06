package com.example.icardbackend.attendance.service;

import com.example.icardbackend.attendance.dto.DailyHoursResponse;
import com.example.icardbackend.attendance.dto.DashboardStatsResponse;
import com.example.icardbackend.attendance.dto.WeeklyChartResponse;
import com.example.icardbackend.attendance.entity.Attendance;
import com.example.icardbackend.attendance.repository.AttendanceRepository;
import com.example.icardbackend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AttendanceRepository attendanceRepository;

    public DashboardStatsResponse getStats(User user) {

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);

        LocalDateTime weekStart = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();
        LocalDateTime weekEnd = weekStart.plusWeeks(1);

        Long todayMinutes = attendanceRepository.sumWorkedMinutesBetween(user, todayStart, todayEnd);
        Long weeklyMinutes = attendanceRepository.sumWorkedMinutesBetween(user, weekStart, weekEnd);
        Long weeklyCheckIns = attendanceRepository.countCheckInsBetween(user, weekStart, weekEnd);
        Long weeklyCheckOuts = attendanceRepository.countCheckOutsBetween(user, weekStart, weekEnd);
        Long totalMinutes = attendanceRepository.sumAllWorkedMinutes(user);

        return DashboardStatsResponse.builder()
                .todayWorkedHours(minutesToHours(todayMinutes))
                .weeklyWorkedHours(minutesToHours(weeklyMinutes))
                .weeklyCheckInCount(weeklyCheckIns)
                .weeklyCheckOutCount(weeklyCheckOuts)
                .totalWorkedHours(minutesToHours(totalMinutes))
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

        String[] labels = {"Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Pazar"};
        DayOfWeek[] order = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};

        List<DailyHoursResponse> days = new ArrayList<>();
        for (int i = 0; i < order.length; i++) {
            Long minutes = minutesByDay.get(order[i]);
            double hours = Math.round((minutes / 60.0) * 100.0) / 100.0;
            days.add(DailyHoursResponse.builder().day(labels[i]).hours(hours).build());
        }

        return WeeklyChartResponse.builder()
                .weekStart(weekStartDate)
                .weekEnd(weekStartDate.plusDays(6))
                .days(days)
                .build();
    }
}