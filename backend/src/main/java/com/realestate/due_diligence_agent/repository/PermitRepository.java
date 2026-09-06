package com.realestate.due_diligence_agent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realestate.due_diligence_agent.entity.Permit;

public interface PermitRepository
        extends JpaRepository<Permit, Long> {

    List<Permit> findByPropertyId(Long propertyId);
}