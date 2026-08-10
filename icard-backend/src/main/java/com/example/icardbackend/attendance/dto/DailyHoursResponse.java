package com.example.icardbackend.attendance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyHoursResponse {
    private String day;
    private Double hours;
    @JsonProperty("isLeave")
    private boolean isLeave;
}