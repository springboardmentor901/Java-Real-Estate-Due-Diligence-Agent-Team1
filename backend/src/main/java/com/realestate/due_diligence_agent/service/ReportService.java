//package com.realestate.due_diligence_agent.service;
//import org.springframework.stereotype.Service;
//
//import com.realestate.due_diligence_agent.entity.Property;
//import com.realestate.due_diligence_agent.entity.Report;
//import com.realestate.due_diligence_agent.entity.ReportStatus;
//import com.realestate.due_diligence_agent.entity.User;
//import com.realestate.due_diligence_agent.repository.PropertyRepository;
//import com.realestate.due_diligence_agent.repository.ReportRepository;
//
//import lombok.RequiredArgsConstructor;
//
//@Service
//@RequiredArgsConstructor
//public class ReportService {
//
//    private final ReportRepository reportRepository;
//    private final PropertyRepository propertyRepository;
//
//    public Report createReport(Long propertyId, User requestedBy) {
//
//        Property property = propertyRepository.findById(propertyId)
//                .orElseThrow(() ->
//                        new RuntimeException("Property not found with id: " + propertyId));
//
//        Report report = Report.builder()
//                .property(property)
//                .requestedBy(requestedBy)
//                .status(ReportStatus.REQUESTED)
//                .build();
//
//        return reportRepository.save(report);
//    }
//}
package com.realestate.due_diligence_agent.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.Report;
import com.realestate.due_diligence_agent.entity.ReportStatus;
import com.realestate.due_diligence_agent.entity.User;
import com.realestate.due_diligence_agent.repository.PropertyRepository;
import com.realestate.due_diligence_agent.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final PropertyRepository propertyRepository;

    public Report createReport(Long propertyId, User requestedBy) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Property not found with id: " + propertyId));

        Report report = Report.builder()
                .property(property)
                .requestedBy(requestedBy)
                .status(ReportStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();

        return reportRepository.save(report);
    }
}