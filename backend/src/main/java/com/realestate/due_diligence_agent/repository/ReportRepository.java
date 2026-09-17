package com.realestate.due_diligence_agent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realestate.due_diligence_agent.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByRequestedById(Long userId);

}