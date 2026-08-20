package com.realestate.due_diligence_agent.controller;

import com.realestate.due_diligence_agent.client.GeocodingClient;
import com.realestate.due_diligence_agent.dto.AddressValidationRequest;
import com.realestate.due_diligence_agent.dto.AddressValidationResponse;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final GeocodingClient geocodingClient;

    public PropertyController(
            PropertyService propertyService,
            GeocodingClient geocodingClient) {

        this.propertyService = propertyService;
        this.geocodingClient = geocodingClient;
    }

    @GetMapping
    public List<Property> getAllProperties() {
        return propertyService.getAllProperties();
    }

    @GetMapping("/search")
    public List<Property> searchProperties(
            @RequestParam String address) {

        return propertyService.searchByAddress(address);
    }
    @PostMapping("/validate_address")
    public AddressValidationResponse validateAddress(
            @Valid @RequestBody AddressValidationRequest request) {

        return geocodingClient.validateAddress(request.getAddress());
    }

    
}