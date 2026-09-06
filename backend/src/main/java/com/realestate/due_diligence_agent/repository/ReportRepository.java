package com.realestate.due_diligence_agent.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realestate.due_diligence_agent.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
}