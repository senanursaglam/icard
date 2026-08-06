package com.example.icardbackend.qr.entity;

import com.example.icardbackend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "QrCode")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QRCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime createdDate;

    private LocalDateTime expireDate;

    private Boolean used;

    private Boolean active;
}