package com.example.icardbackend.attendance.repository;

import com.example.icardbackend.attendance.entity.Attendance;
import com.example.icardbackend.attendance.specification.AttendanceSpecificationBuilder;
import com.example.icardbackend.common.specification.FilterCriteria;
import com.example.icardbackend.user.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    default Specification<Attendance> buildForManagerTeam(List<User> teamMembers, List<FilterCriteria> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("user").in(teamMembers));

            if (filters != null) {
                for (FilterCriteria criteria : filters) {
                    Predicate predicate = AttendanceSpecificationBuilder.toPredicate(root, cb, criteria);
                    if (predicate != null) predicates.add(predicate);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Query("SELECT COALESCE(SUM(a.workedMinutes), 0) FROM Attendance a " +
            "WHERE a.user IN :users AND a.workedMinutes IS NOT NULL")
    Long sumAllWorkedMinutesForUsers(@Param("users") List<User> users);

    @Query("SELECT COALESCE(SUM(a.workedMinutes), 0) FROM Attendance a " +
            "WHERE a.user IN :users AND a.loginTime >= :start AND a.loginTime < :end " +
            "AND a.workedMinutes IS NOT NULL")
    Long sumWorkedMinutesForUsersBetween(@Param("users") List<User> users,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.user = :user " +
            "AND a.isLate = true AND a.loginTime >= :start AND a.loginTime < :end")
    long countLateBetween(@Param("user") User user,
                          @Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(a.overtimeMinutes), 0) FROM Attendance a WHERE a.user = :user " +
            "AND a.overtimeMinutes IS NOT NULL AND a.loginTime >= :start AND a.loginTime < :end")
    Long sumOvertimeMinutesBetween(@Param("user") User user,
                                   @Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);


    @Query("SELECT COALESCE(SUM(a.overtimeMinutes), 0) FROM Attendance a " +
            "WHERE a.user = :user AND a.overtimeMinutes IS NOT NULL")
    Long sumAllOvertimeMinutesForUser(@Param("user") User user);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.user = :user AND a.isLate = true")
    long countAllLateForUser(@Param("user") User user);

    List<Attendance> findByUserInAndLoginTimeBetween(List<User> users, LocalDateTime start, LocalDateTime end);

    List<Attendance> findByUserOrderByLoginTimeDesc(User user, Pageable pageable);
}


