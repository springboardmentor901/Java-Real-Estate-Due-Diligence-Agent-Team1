package com.realestate.due_diligence_agent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realestate.due_diligence_agent.entity.TaxHistory;

public interface TaxHistoryRepository
        extends JpaRepository<TaxHistory, Long> {

    List<TaxHistory> findByPropertyId(Long propertyId);
}