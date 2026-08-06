package com.example.icardbackend.user.controller;

import com.example.icardbackend.security.userdetails.CustomUserDetails;
import com.example.icardbackend.user.dto.UserResponse;
import com.example.icardbackend.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserMapper userMapper;

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userMapper.toResponse(userDetails.getUser());
    }
}