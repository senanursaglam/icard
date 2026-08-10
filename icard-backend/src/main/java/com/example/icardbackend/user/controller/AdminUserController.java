package com.example.icardbackend.user.controller;

import com.example.icardbackend.user.dto.AdminUserCreateRequest;
import com.example.icardbackend.user.dto.AdminUserUpdateRequest;
import com.example.icardbackend.user.dto.UserResponse;
import com.example.icardbackend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody AdminUserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.adminCreateUser(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody AdminUserUpdateRequest request) {
        return ResponseEntity.ok(userService.adminUpdateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        userService.adminDeactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return userService.adminGetAllUsers();
    }
}