package com.realestate.due_diligence_agent.repository;

import com.realestate.due_diligence_agent.entity.FloodZoneData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FloodZoneDataRepository extends JpaRepository<FloodZoneData, Long> {
    List<FloodZoneData> findByPropertyId(Long propertyId);
}
