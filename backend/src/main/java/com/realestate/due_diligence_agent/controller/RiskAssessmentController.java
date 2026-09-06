package com.realestate.due_diligence_agent.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.realestate.due_diligence_agent.entity.Report;
import com.realestate.due_diligence_agent.entity.RiskAssessment;
import com.realestate.due_diligence_agent.service.RiskAssessmentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class RiskAssessmentController {

    private final RiskAssessmentService riskAssessmentService;

    @PostMapping("/{id}/risk-assessment")
    public ResponseEntity<RiskAssessment> assessTaxDue(
            @PathVariable Long id,
            @RequestParam Long reportId) {

        RiskAssessment assessment =
                riskAssessmentService.assessTaxDue(id, reportId);

        return ResponseEntity.ok(assessment);
    }
    @PostMapping("/{id}/risk-assessment/overall")
public ResponseEntity<Report> calculateOverallRisk(
        @PathVariable Long id,
        @RequestParam Long reportId) {

    Report report =
            riskAssessmentService.calculateOverallRisk(id, reportId);

    return ResponseEntity.ok(report);
}
}