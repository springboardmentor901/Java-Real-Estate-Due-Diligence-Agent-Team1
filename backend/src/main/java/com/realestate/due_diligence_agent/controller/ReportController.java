package com.realestate.due_diligence_agent.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realestate.due_diligence_agent.dto.ReportResponse;
import com.realestate.due_diligence_agent.entity.Report;
import com.realestate.due_diligence_agent.entity.User;
import com.realestate.due_diligence_agent.service.ReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/{id}/reports")
    public ResponseEntity<ReportResponse> createReport(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        Report report = reportService.createReport(id, user);

        ReportResponse response = new ReportResponse(report.getId());

        return ResponseEntity.ok(response);
    }
}