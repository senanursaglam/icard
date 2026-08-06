package com.example.icardbackend.qr.repository;

import com.example.icardbackend.qr.entity.QRCode;
import com.example.icardbackend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QRCodeRepository extends JpaRepository<QRCode, Long> {

    Optional<QRCode> findByCodeAndActiveTrue(String code);

    List<QRCode> findAllByUserAndActiveTrue(User user);
}