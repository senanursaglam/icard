package com.example.icardbackend.attendance.dto;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ActiveStatusResponse {
    private boolean active;
}
