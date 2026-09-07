package com.realestate.due_diligence_agent.service;

import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.UtilityInformation;
import com.realestate.due_diligence_agent.repository.PropertyRepository;
import com.realestate.due_diligence_agent.repository.UtilityInformationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UtilityInformationService {

    private final UtilityInformationRepository utilityInformationRepository;
    private final PropertyRepository propertyRepository;

    public UtilityInformationService(UtilityInformationRepository utilityInformationRepository,
                                     PropertyRepository propertyRepository) {
        this.utilityInformationRepository = utilityInformationRepository;
        this.propertyRepository = propertyRepository;
    }

    public List<UtilityInformation> getUtilities(Long propertyId) {
        return utilityInformationRepository.findByPropertyId(propertyId);
    }

    public UtilityInformation createUtility(Long propertyId, UtilityInformation utilityInformation, String username) {
        Optional<Property> propertyOpt = propertyRepository.findById(propertyId);
        if (propertyOpt.isEmpty()) {
            throw new IllegalArgumentException("Property not found");
        }

        utilityInformation.setProperty(propertyOpt.get());
        utilityInformation.setSource(username != null ? username : "manual entry");
        return utilityInformationRepository.save(utilityInformation);
    }

    public UtilityInformation updateUtility(Long utilityId, UtilityInformation updatedInfo, String username) {
        Optional<UtilityInformation> existingOpt = utilityInformationRepository.findById(utilityId);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Utility information not found");
        }

        UtilityInformation existing = existingOpt.get();
        if (updatedInfo.getUtilityType() != null) existing.setUtilityType(updatedInfo.getUtilityType());
        if (updatedInfo.getProvider() != null) existing.setProvider(updatedInfo.getProvider());
        if (updatedInfo.getStatus() != null) existing.setStatus(updatedInfo.getStatus());
        existing.setSource(username != null ? username : "manual entry");

        return utilityInformationRepository.save(existing);
    }

    public void deleteUtility(Long utilityId) {
        utilityInformationRepository.deleteById(utilityId);
    }
}
