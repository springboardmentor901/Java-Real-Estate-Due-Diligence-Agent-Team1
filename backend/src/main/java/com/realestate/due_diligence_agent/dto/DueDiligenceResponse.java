package com.realestate.due_diligence_agent.dto;

import java.util.List;

import com.realestate.due_diligence_agent.entity.EnvironmentalRecord;
import com.realestate.due_diligence_agent.entity.FloodZoneData;
import com.realestate.due_diligence_agent.entity.OwnershipRecord;
import com.realestate.due_diligence_agent.entity.Permit;
import com.realestate.due_diligence_agent.entity.TaxHistory;
import com.realestate.due_diligence_agent.entity.UtilityInformation;
import com.realestate.due_diligence_agent.entity.ZoningInformation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DueDiligenceResponse {

    private SectionResult<List<OwnershipRecord>> ownership;

private SectionResult<List<TaxHistory>> taxHistory;

private SectionResult<ZoningInformation> zoning;

private SectionResult<FloodZoneData> floodZone;

private SectionResult<List<Permit>> permits;

private SectionResult<List<EnvironmentalRecord>> environmental;

private SectionResult<List<UtilityInformation>> utilities;
}