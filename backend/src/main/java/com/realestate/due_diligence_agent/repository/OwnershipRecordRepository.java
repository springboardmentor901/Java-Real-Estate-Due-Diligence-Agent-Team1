package com.realestate.due_diligence_agent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realestate.due_diligence_agent.entity.OwnershipRecord;

public interface OwnershipRecordRepository
        extends JpaRepository<OwnershipRecord, Long> {

    List<OwnershipRecord> findByPropertyId(Long propertyId);
}