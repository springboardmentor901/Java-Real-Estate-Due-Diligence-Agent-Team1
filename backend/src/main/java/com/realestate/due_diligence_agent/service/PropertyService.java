package com.realestate.due_diligence_agent.service;

import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.PropertyHistory;
import com.realestate.due_diligence_agent.repository.PropertyHistoryRepository;
import com.realestate.due_diligence_agent.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyHistoryRepository propertyHistoryRepository;

    public PropertyService(PropertyRepository propertyRepository , PropertyHistoryRepository propertyHistoryRepository) {
        this.propertyRepository = propertyRepository;
        this.propertyHistoryRepository = propertyHistoryRepository;
    }

    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    public List<Property> searchByAddress(String address) {
        return propertyRepository.findByAddressContainingIgnoreCase(address);
    }

    public Property getPropertyById(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Property not found with id: " + id));
    }

    public List<PropertyHistory> getPropertyHistory(Long propertyId) {
        getPropertyById(propertyId);
        return propertyHistoryRepository.findByPropertyId(propertyId);
    }
}