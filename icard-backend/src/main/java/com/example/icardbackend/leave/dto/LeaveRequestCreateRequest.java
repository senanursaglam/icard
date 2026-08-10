package com.example.icardbackend.leave.dto;

import com.example.icardbackend.leave.entity.LeaveType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestCreateRequest {

    @NotNull(message = "Başlangıç tarihi boş olamaz")
    private LocalDate startDate;

    @NotNull(message = "Bitiş tarihi boş olamaz")
    private LocalDate endDate;

    @NotNull(message = "İzin türü boş olamaz")
    private LeaveType leaveType;

    private String reason;
}