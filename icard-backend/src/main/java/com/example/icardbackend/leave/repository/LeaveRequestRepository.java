package com.example.icardbackend.leave.repository;

import com.example.icardbackend.leave.entity.LeaveRequest;
import com.example.icardbackend.leave.entity.LeaveStatus;
import com.example.icardbackend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByUser(User user);

    Page<LeaveRequest> findByUserIn(List<User> users, Pageable pageable);
    Page<LeaveRequest> findByUserInAndStatus(List<User> users, LeaveStatus status, Pageable pageable);


    // Çakışma kontrolü: aynı kullanıcının, verilen aralıkla kesişen ve
    // PENDING/APPROVED durumunda olan bir izni var mı?
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user = :user " +
            "AND lr.status IN ('PENDING', 'APPROVED') " +
            "AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    List<LeaveRequest> findOverlapping(@Param("user") User user,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    // Belirli bir tarih aralığında, verilen kullanıcıların ONAYLANMIŞ izin günlerini bulmak için
    // (Dashboard hesaplamasında kullanılacak)
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user = :user " +
            "AND lr.status = 'APPROVED' " +
            "AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    List<LeaveRequest> findApprovedOverlapping(@Param("user") User user,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
}