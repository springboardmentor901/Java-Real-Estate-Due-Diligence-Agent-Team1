package com.realestate.due_diligence_agent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realestate.due_diligence_agent.entity.UtilityInformation;

public interface UtilityInformationRepository
        extends JpaRepository<UtilityInformation, Long> {

    List<UtilityInformation> findByPropertyId(Long propertyId);
}