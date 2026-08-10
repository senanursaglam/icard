package com.example.icardbackend.user.mapper;

import com.example.icardbackend.user.dto.RegisterRequest;
import com.example.icardbackend.user.dto.UserResponse;
import com.example.icardbackend.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterRequest request, String encodedPassword) { //kullanıcıdan gelen
        // bilgi ile bir User oluşturur
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(encodedPassword) //user serviceden hashlenmiş passwordu alır
                .build();
    }
    public UserResponse toResponse(User user) { //User entitysini frontende direkt göndermek
        // yerine UserResponsea çeviriyoruz
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .createdDate(user.getCreatedDate())
                .role(user.getRole())
                .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .managerName(user.getManager() != null
                        ? user.getManager().getFirstName() + " " + user.getManager().getLastName()
                        : null)
                .active(user.getActive())
                .build();
    }
}