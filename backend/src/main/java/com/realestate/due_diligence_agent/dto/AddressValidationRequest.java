package com.realestate.due_diligence_agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressValidationRequest {

    @NotBlank(message = "Address is required")
    private String address;
}