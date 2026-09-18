package com.realestate.due_diligence_agent.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realestate.due_diligence_agent.entity.Property;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByAddressContainingIgnoreCase(String address);

    Optional<Property> findByAddress(String address);
}