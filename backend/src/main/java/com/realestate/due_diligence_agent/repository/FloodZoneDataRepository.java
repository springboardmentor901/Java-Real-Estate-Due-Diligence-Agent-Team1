package com.realestate.due_diligence_agent.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realestate.due_diligence_agent.entity.FloodZoneData;

public interface FloodZoneDataRepository
        extends JpaRepository<FloodZoneData, Long> {

    Optional<FloodZoneData> findByPropertyId(Long propertyId);
}