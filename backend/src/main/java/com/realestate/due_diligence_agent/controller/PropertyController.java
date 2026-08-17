package com.realestate.due_diligence_agent.controller;

import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.service.PropertyService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.realestate.due_diligence_agent.service.GeocodingService;
import com.realestate.due_diligence_agent.dto.PropertyRequest;
import jakarta.validation.Valid;
import com.realestate.due_diligence_agent.service.PropertyDetailsService;
import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;
    private final GeocodingService geocodingService;
    private final PropertyDetailsService propertyDetailsService;
   
    @PostMapping
    public Property createProperty(@Valid @RequestBody PropertyRequest request) {

        GeocodingService.GeocodingResult location =
                geocodingService.geocode(request.getAddress());

        Property property = Property.builder()
                .address(request.getAddress())
                .propertyType(request.getPropertyType())
                .bedrooms(request.getBedrooms())
                .bathrooms(request.getBathrooms())
                .squareFeet(request.getSquareFeet())
                .yearBuilt(request.getYearBuilt())
                .latitude(Double.parseDouble(location.getLat()))
                .longitude(Double.parseDouble(location.getLon()))
                .build();

        return propertyService.saveProperty(property);
    }
    @GetMapping
    public List<Property> getAllProperties() {
        return propertyService.getAllProperties();
    }

    @GetMapping("/{id}")
    public Property getPropertyById(@PathVariable Long id) {
        return propertyService.getPropertyById(id);
    }
    @GetMapping("/geocode")
    public GeocodingService.GeocodingResult geocodeAddress(
            @RequestParam String address) {

        return geocodingService.geocode(address);
    }
    @GetMapping("/{id}/details")
    public Property getPropertyDetails(@PathVariable Long id) {
        return propertyDetailsService.getPropertyDetails(id);
    }
}