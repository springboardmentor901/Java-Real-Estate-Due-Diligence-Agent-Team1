package com.realestate.due_diligence_agent.dto;

import java.util.List;
import java.util.Map;

import com.realestate.due_diligence_agent.entity.AuditLog;

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
public class AdminDashboardResponse {

    private long totalUsers;
    private long totalProperties;
    private long totalReports;
    private long reportsLast7Days;
    private List<AuditLog> recentAuditLogs;
    private Map<String, Long> userRoles;
}
