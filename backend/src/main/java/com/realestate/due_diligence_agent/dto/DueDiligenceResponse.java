package com.realestate.due_diligence_agent.dto;

import com.realestate.due_diligence_agent.entity.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DueDiligenceResponse {
    private List<OwnershipRecord> ownership;
    private List<TaxHistory> taxHistory;
    private List<ZoningInformation> zoning;
    private List<FloodZoneData> floodZone;
    private List<Permit> permits;
    private List<EnvironmentalRecord> environmental;
    private List<UtilityInformation> utilities;
}
