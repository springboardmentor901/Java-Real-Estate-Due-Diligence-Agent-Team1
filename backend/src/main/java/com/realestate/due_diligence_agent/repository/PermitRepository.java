package com.realestate.due_diligence_agent.repository;

import com.realestate.due_diligence_agent.entity.Permit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermitRepository extends JpaRepository<Permit, Long> {
    List<Permit> findByPropertyId(Long propertyId);
}
