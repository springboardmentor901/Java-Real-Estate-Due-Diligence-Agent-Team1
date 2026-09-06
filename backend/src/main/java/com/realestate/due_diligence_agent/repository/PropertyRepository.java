package com.realestate.due_diligence_agent.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realestate.due_diligence_agent.entity.Property;

public interface PropertyRepository extends JpaRepository<Property, Long> {
}