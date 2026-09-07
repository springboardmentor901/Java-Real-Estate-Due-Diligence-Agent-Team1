package com.realestate.due_diligence_agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyRequest {

    @NotBlank(message = "Address is required")
    private String address;

    private String propertyType;

    private Integer bedrooms;

    private Double bathrooms;

    private Double squareFeet;

    private Integer yearBuilt;
}