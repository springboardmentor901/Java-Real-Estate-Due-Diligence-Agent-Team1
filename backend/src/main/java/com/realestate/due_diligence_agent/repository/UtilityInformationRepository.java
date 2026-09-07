package com.realestate.due_diligence_agent.repository;

import com.realestate.due_diligence_agent.entity.UtilityInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UtilityInformationRepository extends JpaRepository<UtilityInformation, Long> {
    List<UtilityInformation> findByPropertyId(Long propertyId);
}
