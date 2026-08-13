package com.example.icardbackend.user.dto;

import com.example.icardbackend.common.specification.FilterCriteria;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserFilterRequest {

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;

    private List<FilterCriteria> filters;
}