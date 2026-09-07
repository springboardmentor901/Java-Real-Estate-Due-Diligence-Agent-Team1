package com.realestate.due_diligence_agent.dto;

import lombok.Data;

@Data
public class RegridParcelResponse {
    private String parcelId;
    private String zoningCode;
    private String landUseDesc;
}
