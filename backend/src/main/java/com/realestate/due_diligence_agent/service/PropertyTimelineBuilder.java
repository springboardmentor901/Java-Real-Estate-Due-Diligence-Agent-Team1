package com.realestate.due_diligence_agent.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.realestate.due_diligence_agent.dto.PropertyTimelineEntry;
import com.realestate.due_diligence_agent.entity.OwnershipRecord;
import com.realestate.due_diligence_agent.entity.Permit;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.TaxHistory;
import com.realestate.due_diligence_agent.repository.OwnershipRecordRepository;
import com.realestate.due_diligence_agent.repository.PermitRepository;
import com.realestate.due_diligence_agent.repository.TaxHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PropertyTimelineBuilder {

    private final OwnershipRecordRepository ownershipRecordRepository;
    private final TaxHistoryRepository taxHistoryRepository;
    private final PermitRepository permitRepository;

    public List<PropertyTimelineEntry> build(Property property) {

        List<PropertyTimelineEntry> timeline = new ArrayList<>();

        addOwnershipEvents(property, timeline);
        addTaxEvents(property, timeline);
        addPermitEvents(property, timeline);

        timeline.sort(
                Comparator.comparing(
                        PropertyTimelineEntry::getDate,
                        Comparator.nullsLast(Comparator.naturalOrder())
                )
        );

        return timeline;
    }

    private void addOwnershipEvents(
            Property property,
            List<PropertyTimelineEntry> timeline) {

        List<OwnershipRecord> records = property.getOwnershipRecords();

        if (records == null || records.isEmpty()) {
            if (property.getId() != null) {
                records = ownershipRecordRepository.findByPropertyId(property.getId());
            }
        }

        if (records == null) {
            return;
        }

        for (OwnershipRecord record : records) {

            if (record.getAcquisitionDate() == null) {
                continue;
            }

            String ownerName = record.getOwnerName() != null
                    ? record.getOwnerName()
                    : "Unknown owner";

            String ownershipType = record.getOwnershipType() != null
                    ? record.getOwnershipType()
                    : "Unknown ownership type";

            timeline.add(
                    new PropertyTimelineEntry(
                            record.getAcquisitionDate(),
                            "Ownership",
                            "Property acquired by "
                                    + ownerName
                                    + " ("
                                    + ownershipType
                                    + ")."
                    )
            );
        }
    }

    private void addTaxEvents(
            Property property,
            List<PropertyTimelineEntry> timeline) {

        List<TaxHistory> taxHistories = property.getTaxHistories();

        if (taxHistories == null || taxHistories.isEmpty()) {
            if (property.getId() != null) {
                taxHistories = taxHistoryRepository.findByPropertyId(property.getId());
            }
        }

        if (taxHistories == null) {
            return;
        }

        for (TaxHistory tax : taxHistories) {

            if (tax.getTaxYear() == null) {
                continue;
            }

            LocalDate date =
                    LocalDate.of(tax.getTaxYear(), 12, 31);

            String status = tax.getPaymentStatus() != null
                    ? tax.getPaymentStatus()
                    : "Unknown";

            timeline.add(
                    new PropertyTimelineEntry(
                            date,
                            "Tax Record",
                            "Property tax record for "
                                    + tax.getTaxYear()
                                    + " has payment status: "
                                    + status
                                    + "."
                    )
            );
        }
    }

    private void addPermitEvents(
            Property property,
            List<PropertyTimelineEntry> timeline) {

        List<Permit> permits = property.getPermits();

        if (permits == null || permits.isEmpty()) {
            if (property.getId() != null) {
                permits = permitRepository.findByPropertyId(property.getId());
            }
        }

        if (permits == null) {
            return;
        }

        for (Permit permit : permits) {

            if (permit.getIssuedDate() == null) {
                continue;
            }

            String permitType = permit.getPermitType() != null
                    ? permit.getPermitType()
                    : "Unknown permit type";

            String permitNumber = permit.getPermitNumber() != null
                    ? permit.getPermitNumber()
                    : "Unknown permit number";

            String status = permit.getStatus() != null
                    ? permit.getStatus()
                    : "Unknown";

            timeline.add(
                    new PropertyTimelineEntry(
                            permit.getIssuedDate(),
                            "Permit",
                            "Permit "
                                    + permitNumber
                                    + " ("
                                    + permitType
                                    + ") has status: "
                                    + status
                                    + "."
                    )
            );
        }
    }
}