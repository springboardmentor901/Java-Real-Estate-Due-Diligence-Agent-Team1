package com.realestate.due_diligence_agent.repository;

import com.realestate.due_diligence_agent.entity.TaxHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxHistoryRepository extends JpaRepository<TaxHistory, Long> {
    List<TaxHistory> findByPropertyId(Long propertyId);
}
