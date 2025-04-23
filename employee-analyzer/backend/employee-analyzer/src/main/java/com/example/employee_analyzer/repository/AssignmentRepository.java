package com.example.employee_analyzer.repository;

import com.example.employee_analyzer.model.entity.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentRepository extends JpaRepository<AssignmentEntity, Integer> {
}
