package com.realestate.due_diligence_agent.repository;

import com.realestate.due_diligence_agent.entity.OwnershipRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OwnershipRecordRepository extends JpaRepository<OwnershipRecord, Long> {
    List<OwnershipRecord> findByPropertyId(Long propertyId);
}
