package com.example.icardbackend.user.repository;

import com.example.icardbackend.department.entity.Department;
import com.example.icardbackend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByManager(User manager);

    List<User> findByDepartment(Department department);

}