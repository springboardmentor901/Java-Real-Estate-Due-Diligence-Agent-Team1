package com.realestate.due_diligence_agent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realestate.due_diligence_agent.entity.EnvironmentalRecord;

public interface EnvironmentalRecordRepository
        extends JpaRepository<EnvironmentalRecord, Long> {

    List<EnvironmentalRecord> findByPropertyId(Long propertyId);
}