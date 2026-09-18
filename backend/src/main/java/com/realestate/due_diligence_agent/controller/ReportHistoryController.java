package com.realestate.due_diligence_agent.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.realestate.due_diligence_agent.entity.Report;
import com.realestate.due_diligence_agent.entity.User;
import com.realestate.due_diligence_agent.service.ReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportHistoryController {

    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<List<Report>> getReportHistory(
            @RequestParam Long user,
            @AuthenticationPrincipal User authenticatedUser) {

        if (!authenticatedUser.getId().equals(user)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
                reportService.getReportsByUser(user)
        );
    }
}