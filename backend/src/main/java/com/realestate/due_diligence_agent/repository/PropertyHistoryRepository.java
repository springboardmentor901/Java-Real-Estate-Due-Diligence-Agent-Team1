package com.realestate.due_diligence_agent.repository;

import com.realestate.due_diligence_agent.entity.PropertyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyHistoryRepository extends JpaRepository<PropertyHistory, Long> {

    List<PropertyHistory> findByPropertyId(Long propertyId);
}