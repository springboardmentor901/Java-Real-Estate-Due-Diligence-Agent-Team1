package com.realestate.due_diligence_agent.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.realestate.due_diligence_agent.entity.ZoningInformation;
import com.realestate.due_diligence_agent.repository.ZoningInformationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ZoningInformationService {

    private final ZoningInformationRepository zoningInformationRepository;

    public Optional<ZoningInformation> getZoningInformation(Long propertyId) {
        return zoningInformationRepository.findByPropertyId(propertyId);
    }
}