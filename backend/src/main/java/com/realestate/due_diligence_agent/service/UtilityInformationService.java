package com.realestate.due_diligence_agent.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.UtilityInformation;
import com.realestate.due_diligence_agent.exception.PropertyNotFoundException;
import com.realestate.due_diligence_agent.repository.PropertyRepository;
import com.realestate.due_diligence_agent.repository.UtilityInformationRepository;

@Service
public class UtilityInformationService {

    private final UtilityInformationRepository utilityInformationRepository;
    private final PropertyRepository propertyRepository;

    public UtilityInformationService(
            UtilityInformationRepository utilityInformationRepository,
            PropertyRepository propertyRepository) {

        this.utilityInformationRepository = utilityInformationRepository;
        this.propertyRepository = propertyRepository;
    }

    // =========================================================
    // CREATE
    // =========================================================

    @Transactional
    public UtilityInformation createUtility(
            Long propertyId,
            UtilityInformation utilityInformation) {

        Property property =
                propertyRepository.findById(propertyId)
                        .orElseThrow(() ->
                                new PropertyNotFoundException(
                                        "Property not found with id: "
                                                + propertyId
                                )
                        );

        utilityInformation.setId(null);
        utilityInformation.setProperty(property);
        utilityInformation.setSource("Manual Entry");

        return utilityInformationRepository.save(
                utilityInformation
        );
    }

    // =========================================================
    // GET ALL
    // =========================================================

    @Transactional(readOnly = true)
    public List<UtilityInformation> getUtilities(
            Long propertyId) {

        // Make sure property exists
        propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new PropertyNotFoundException(
                                "Property not found with id: "
                                        + propertyId
                        )
                );

        return utilityInformationRepository
                .findByPropertyId(propertyId);
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Transactional
    public UtilityInformation updateUtility(
            Long propertyId,
            Long utilityId,
            UtilityInformation updatedUtility) {

        // Make sure property exists
        Property property =
                propertyRepository.findById(propertyId)
                        .orElseThrow(() ->
                                new PropertyNotFoundException(
                                        "Property not found with id: "
                                                + propertyId
                                )
                        );

        UtilityInformation existingUtility =
                utilityInformationRepository.findById(utilityId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Utility information not found with id: "
                                                + utilityId
                                )
                        );

        // Make sure utility belongs to this property
        if (!existingUtility.getProperty()
                .getId()
                .equals(propertyId)) {

            throw new RuntimeException(
                    "Utility information does not belong to property: "
                            + propertyId
            );
        }

        existingUtility.setUtilityType(
                updatedUtility.getUtilityType()
        );

        existingUtility.setProvider(
                updatedUtility.getProvider()
        );

        existingUtility.setStatus(
                updatedUtility.getStatus()
        );

        existingUtility.setProperty(property);

        return utilityInformationRepository.save(
                existingUtility
        );
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Transactional
    public void deleteUtility(
            Long propertyId,
            Long utilityId) {

        // Make sure property exists
        propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new PropertyNotFoundException(
                                "Property not found with id: "
                                        + propertyId
                        )
                );

        UtilityInformation existingUtility =
                utilityInformationRepository.findById(utilityId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Utility information not found with id: "
                                                + utilityId
                                )
                        );

        // Prevent deleting a utility belonging
        // to another property
        if (!existingUtility.getProperty()
                .getId()
                .equals(propertyId)) {

            throw new RuntimeException(
                    "Utility information does not belong to property: "
                            + propertyId
            );
        }

        utilityInformationRepository.delete(
                existingUtility
        );
    }
}