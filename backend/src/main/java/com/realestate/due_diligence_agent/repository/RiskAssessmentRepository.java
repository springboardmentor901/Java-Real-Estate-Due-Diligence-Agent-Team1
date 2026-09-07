package com.realestate.due_diligence_agent.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realestate.due_diligence_agent.entity.RiskAssessment;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
}