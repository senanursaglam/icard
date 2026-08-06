package com.example.icardbackend.attendance.entity;

import com.example.icardbackend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime loginTime;

    private LocalDateTime logoutTime;

    private Long workedMinutes;

    private Double workedHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;
}
