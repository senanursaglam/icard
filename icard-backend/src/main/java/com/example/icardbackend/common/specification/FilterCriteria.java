package com.example.icardbackend.common.specification;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterCriteria
{
    private String field;
    private Object value; //EQ, LIKE için tek değer
    private FilterOperator operator;
    private List<Object> values; //BETWEEN vs için list
}
