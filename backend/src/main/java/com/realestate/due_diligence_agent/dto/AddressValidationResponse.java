package com.realestate.due_diligence_agent.dto;



import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressValidationResponse {

    private boolean valid;
    private String formattedAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
}