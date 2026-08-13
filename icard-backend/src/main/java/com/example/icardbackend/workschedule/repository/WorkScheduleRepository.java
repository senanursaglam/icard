package com.example.icardbackend.workschedule.repository;

import com.example.icardbackend.department.entity.Department;
import com.example.icardbackend.workschedule.entity.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
    Optional<WorkSchedule> findByDepartment(Department department);
    Optional<WorkSchedule> findByDepartmentIsNull(); // şirket geneli varsayılan
}