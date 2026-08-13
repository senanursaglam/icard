package com.example.icardbackend.user.specification;

import com.example.icardbackend.common.specification.FilterCriteria;
import com.example.icardbackend.user.entity.RoleType;
import com.example.icardbackend.user.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserSpecificationBuilder {

    public Specification<User> build(List<FilterCriteria> filters) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filters != null) {
                for (FilterCriteria criteria : filters) {
                    String field = criteria.getField();

                    switch (criteria.getOperator()) {
                        case EQ -> {
                            if (field.equals("role")) {
                                predicates.add(cb.equal(root.get("role"),
                                        RoleType.valueOf(criteria.getValue().toString())));
                            } else if (field.equals("departmentId")) {
                                predicates.add(cb.equal(
                                        root.join("department").get("id"),
                                        Long.valueOf(criteria.getValue().toString())));
                            } else if (field.equals("active")) {
                                predicates.add(cb.equal(root.get("active"),
                                        Boolean.valueOf(criteria.getValue().toString())));
                            } else {
                                predicates.add(cb.equal(root.get(field), criteria.getValue()));
                            }
                        }
                        case LIKE -> predicates.add(cb.like(
                                cb.lower(root.get(field).as(String.class)),
                                "%" + criteria.getValue().toString().toLowerCase() + "%"
                        ));
                        default -> {
                            // diğer operatörler bu ekranda kullanılmıyor, gerekirse genişletilir
                        }
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}