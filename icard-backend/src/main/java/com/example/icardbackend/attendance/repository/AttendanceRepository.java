package com.example.icardbackend.attendance.repository;

import com.example.icardbackend.attendance.entity.Attendance;
import com.example.icardbackend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>,
        JpaSpecificationExecutor<Attendance> {
    //çıkış yapmadıysa statusu activedir
    Optional<Attendance> findByUserAndLogoutTimeIsNull (User user);
    Page<Attendance> findByUser(User user, Pageable pageable);

    // belirli bir tarih aralığındaki toplam çalışılan dakikayı toplar
    @Query("SELECT COALESCE(SUM(a.workedMinutes), 0) FROM Attendance a " +
            "WHERE a.user = :user AND a.loginTime >= :start AND a.loginTime < :end " +
            "AND a.workedMinutes IS NOT NULL")
    Long sumWorkedMinutesBetween(@Param("user") User user,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);

    // belirli bir tarih aralığındaki toplam giriş sayısı
    @Query("SELECT COUNT(a) FROM Attendance a " +
            "WHERE a.user = :user AND a.loginTime >= :start AND a.loginTime < :end")
    Long countCheckInsBetween(@Param("user") User user,
                              @Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);

    // belirli bir tarih aralığındaki toplam çıkış sayısı (logoutTime dolu olanlar)
    @Query("SELECT COUNT(a) FROM Attendance a " +
            "WHERE a.user = :user AND a.logoutTime IS NOT NULL " +
            "AND a.logoutTime >= :start AND a.logoutTime < :end")
    Long countCheckOutsBetween(@Param("user") User user,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    // kullanıcının şimdiye kadarki tüm çalışma süresi toplamı
    @Query("SELECT COALESCE(SUM(a.workedMinutes), 0) FROM Attendance a " +
            "WHERE a.user = :user AND a.workedMinutes IS NOT NULL")
    Long sumAllWorkedMinutes(@Param("user") User user);

    List<Attendance> findByUserAndLoginTimeBetween(User user, LocalDateTime start, LocalDateTime end);
}


