package com.example.icardbackend.attendance.specification;

import com.example.icardbackend.attendance.entity.Attendance;
import com.example.icardbackend.common.specification.FilterCriteria;
import com.example.icardbackend.user.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class AttendanceSpecificationBuilder {

    public Specification<Attendance> build(User user, List<FilterCriteria> filters) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // güvenlik koşulu
            predicates.add(cb.equal(root.get("user"), user));

            if (filters != null) {
                for (FilterCriteria criteria : filters) {
                    Predicate predicate = toPredicate(root, cb, criteria);
                    if (predicate != null) {
                        predicates.add(predicate);
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Predicate toPredicate(jakarta.persistence.criteria.Root<Attendance> root,
                                  jakarta.persistence.criteria.CriteriaBuilder cb,
                                  FilterCriteria criteria) {

        String field = criteria.getField();

        return switch (criteria.getOperator()) {

            case EQ -> {
                if (field.equals("status")) {
                    yield cb.equal(root.get(field), Enum.valueOf(
                            com.example.icardbackend.attendance.entity.AttendanceStatus.class,
                            criteria.getValue().toString()));
                }
                yield cb.equal(root.get(field), criteria.getValue());
            }

            case LIKE -> cb.like(
                    cb.lower(root.get(field).as(String.class)),
                    "%" + criteria.getValue().toString().toLowerCase() + "%"
            );

            case IN -> root.get(field).in(criteria.getValues());

            case BETWEEN -> cb.between(
                    root.get(field),
                    (Comparable) criteria.getValues().get(0),
                    (Comparable) criteria.getValues().get(1)
            );

            case DATE_BETWEEN -> {
                LocalDate startDate = LocalDate.parse(criteria.getValues().get(0).toString());
                LocalDate endDate = LocalDate.parse(criteria.getValues().get(1).toString());

                LocalDateTime start = startDate.atStartOfDay();
                LocalDateTime end = endDate.plusDays(1).atStartOfDay(); // gün sonuna kadar dahil

                yield cb.between(root.get(field), start, end);
            }
        };
    }
    public Specification<Attendance> buildForTeam(List<User> teamMembers, List<FilterCriteria> filters) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("user").in(teamMembers));

            if (filters != null) {
                for (FilterCriteria criteria : filters) {
                    Predicate predicate = toPredicate(root, cb, criteria);
                    if (predicate != null) {
                        predicates.add(predicate);
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}