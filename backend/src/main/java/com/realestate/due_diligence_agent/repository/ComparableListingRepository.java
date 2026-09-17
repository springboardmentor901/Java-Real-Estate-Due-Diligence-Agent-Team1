package com.realestate.due_diligence_agent.repository;

import com.realestate.due_diligence_agent.entity.ComparableListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComparableListingRepository
        extends JpaRepository<ComparableListing, Long> {

    List<ComparableListing> findByPropertyId(Long propertyId);
}