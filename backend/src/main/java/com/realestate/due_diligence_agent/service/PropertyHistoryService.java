package com.realestate.due_diligence_agent.service;

import com.realestate.due_diligence_agent.dto.PropertyTimelineEntry;
import com.realestate.due_diligence_agent.entity.Property;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyHistoryService {

    private final PropertyService propertyService;
    private final PropertyTimelineBuilder propertyTimelineBuilder;
    private final OwnershipService ownershipService;

    @Transactional
    public List<PropertyTimelineEntry> getPropertyHistory(Long propertyId) {

        Property property = propertyService.getPropertyById(propertyId);

        try {
            ownershipService.getOwnershipRecords(propertyId);
        } catch (Exception ignored) {}

        return propertyTimelineBuilder.build(property);
    }
}