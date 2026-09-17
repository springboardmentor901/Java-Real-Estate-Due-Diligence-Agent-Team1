package com.realestate.due_diligence_agent.dto;

import java.time.LocalDateTime;

import com.realestate.due_diligence_agent.entity.ReportStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportDetailResponse {

    private Long id;
    private Long propertyId;
    private Long requestedBy;
    private Integer riskScore;
    private String executiveSummary;
    private String propertyTimeline;
    private String pdfUrl;
    private String excelUrl;
    private ReportStatus status;
    private LocalDateTime createdAt;
}