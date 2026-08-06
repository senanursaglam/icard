package com.example.icardbackend.attendance.dto;

import com.example.icardbackend.common.specification.FilterCriteria;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceFilterRequest {

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;

    private String sort; // örn: "loginTime,desc"

    private List<FilterCriteria> filters;
}