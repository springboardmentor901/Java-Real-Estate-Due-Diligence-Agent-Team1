package com.realestate.due_diligence_agent.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.repository.PropertyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final GeocodingService geocodingService;
    private final PropertyDetailsService propertyDetailsService;

    public Property saveProperty(Property property) {
        return propertyRepository.save(property);
    }

    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    public Property getPropertyById(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Property not found with id: " + id));
    }

    @Transactional
    public List<Property> searchProperties(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        String trimmedQuery = query.trim();

        // 1. Search existing database properties
        List<Property> localMatches =
                propertyRepository.findByAddressContainingIgnoreCase(trimmedQuery);

        if (!localMatches.isEmpty()) {
            Set<Long> seenIds = new HashSet<>();
            List<Property> distinctLocal = new ArrayList<>();
            for (Property p : localMatches) {
                if (p.getId() != null && seenIds.add(p.getId())) {
                    distinctLocal.add(p);
                }
            }
            return distinctLocal;
        }

        // 2. Perform live external address search
        List<GeocodingService.GeocodingResult> geoResults =
                geocodingService.searchLocations(trimmedQuery, 5);

        if (geoResults.isEmpty()) {
            return Collections.emptyList();
        }

        List<Property> results = new ArrayList<>();
        Set<Long> seenIds = new HashSet<>();
        Set<String> seenAddresses = new HashSet<>();

        for (GeocodingService.GeocodingResult geo : geoResults) {
            String address = geo.getDisplay_name();
            if (address == null || address.isBlank()) {
                continue;
            }

            String normalizedAddress = address.trim();
            if (seenAddresses.contains(normalizedAddress.toLowerCase())) {
                continue;
            }
            seenAddresses.add(normalizedAddress.toLowerCase());

            // Check if already in database
            Optional<Property> existing = propertyRepository.findByAddress(normalizedAddress);
            Property targetProperty;
            if (existing.isPresent()) {
                targetProperty = existing.get();
            } else {
                double lat = 0.0;
                double lon = 0.0;
                try {
                    lat = Double.parseDouble(geo.getLat());
                    lon = Double.parseDouble(geo.getLon());
                } catch (Exception ignored) {}

                Property newProperty = Property.builder()
                        .address(normalizedAddress)
                        .latitude(lat)
                        .longitude(lon)
                        .build();

                // Enrich basic property details from configured external property-record aggregator
                newProperty = propertyDetailsService.enrichPropertyFromExternal(newProperty);

                targetProperty = propertyRepository.save(newProperty);
            }

            if (targetProperty.getId() != null && seenIds.add(targetProperty.getId())) {
                results.add(targetProperty);
            }
        }

        return results;
    }
}