package com.realestate.due_diligence_agent.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.realestate.due_diligence_agent.dto.AdminDashboardResponse;
import com.realestate.due_diligence_agent.entity.AuditLog;
import com.realestate.due_diligence_agent.entity.Report;
import com.realestate.due_diligence_agent.entity.Role;
import com.realestate.due_diligence_agent.entity.User;
import com.realestate.due_diligence_agent.repository.AuditLogRepository;
import com.realestate.due_diligence_agent.repository.PropertyRepository;
import com.realestate.due_diligence_agent.repository.ReportRepository;
import com.realestate.due_diligence_agent.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMINISTRATOR')")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final ReportRepository reportRepository;
    private final AuditLogRepository auditLogRepository;

    @PostConstruct
    public void initAuditLogs() {
        try {
            if (auditLogRepository.count() == 0) {
                log.info("Initializing Audit Log history from existing database records...");

                // 1. Audit user registrations
                List<User> users = userRepository.findAll();
                for (User u : users) {
                    AuditLog regLog = AuditLog.builder()
                            .userId(u.getId())
                            .userEmail(u.getEmail())
                            .action("USER_REGISTRATION")
                            .details("Account registered with role: " + u.getRole().name())
                            .timestamp(u.getCreatedAt() != null ? u.getCreatedAt() : LocalDateTime.now().minusDays(15))
                            .build();
                    auditLogRepository.save(regLog);
                }

                // 2. Audit historical reports
                List<Report> reports = reportRepository.findAll();
                for (Report r : reports) {
                    String propAddress = (r.getProperty() != null) ? r.getProperty().getAddress() : "Property #" + r.getId();
                    String userEmail = (r.getRequestedBy() != null) ? r.getRequestedBy().getEmail() : "system@realestateapp.com";
                    Long userId = (r.getRequestedBy() != null) ? r.getRequestedBy().getId() : null;

                    AuditLog repLog = AuditLog.builder()
                            .userId(userId)
                            .userEmail(userEmail)
                            .action("REPORT_GENERATION")
                            .details("Generated " + r.getStatus() + " report for " + propAddress + " (Risk Score: " + (r.getRiskScore() != null ? r.getRiskScore() : "N/A") + ")")
                            .timestamp(r.getCreatedAt() != null ? r.getCreatedAt() : LocalDateTime.now().minusDays(5))
                            .build();
                    auditLogRepository.save(repLog);
                }

                log.info("Audit Log history initialized with {} entries.", auditLogRepository.count());
            }
        } catch (Exception e) {
            log.warn("Failed to initialize audit logs: {}", e.getMessage());
        }
    }

    // =====================================================
    // GET ADMIN DASHBOARD METRICS
    // =====================================================
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboard() {
        long totalUsers = userRepository.count();
        long totalProperties = propertyRepository.count();
        long totalReports = reportRepository.count();
        long reportsLast7Days = reportRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(7));

        List<AuditLog> recentAuditLogs = auditLogRepository.findTop20ByOrderByTimestampDesc();

        Map<String, Long> userRoles = new HashMap<>();
        List<User> allUsers = userRepository.findAll();
        for (Role r : Role.values()) {
            long count = allUsers.stream().filter(u -> u.getRole() == r).count();
            userRoles.put(r.name(), count);
        }

        AdminDashboardResponse response = AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalProperties(totalProperties)
                .totalReports(totalReports)
                .reportsLast7Days(reportsLast7Days)
                .recentAuditLogs(recentAuditLogs)
                .userRoles(userRoles)
                .build();

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // GET AUDIT LOGS (FILTERED & PAGINATED)
    // =====================================================
    @GetMapping("/audit-logs")
    public ResponseEntity<Map<String, Object>> getAuditLogs(
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startDate != null && !startDate.isBlank()) {
            try {
                startDateTime = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE).atStartOfDay();
            } catch (Exception e) {
                try {
                    startDateTime = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME);
                } catch (Exception ignored) {}
            }
        }

        if (endDate != null && !endDate.isBlank()) {
            try {
                endDateTime = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE).atTime(23, 59, 59);
            } catch (Exception e) {
                try {
                    endDateTime = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME);
                } catch (Exception ignored) {}
            }
        }

        final LocalDateTime finalStart = startDateTime;
        final LocalDateTime finalEnd = endDateTime;

        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (user != null && !user.trim().isEmpty()) {
                String pattern = "%" + user.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("userEmail")), pattern));
            }

            if (action != null && !action.trim().isEmpty() && !action.equalsIgnoreCase("ALL")) {
                predicates.add(cb.equal(root.get("action"), action.trim()));
            }

            if (finalStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), finalStart));
            }

            if (finalEnd != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), finalEnd));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.max(1, Math.min(size, 100)),
                Sort.by(Sort.Direction.DESC, "timestamp")
        );

        Page<AuditLog> logPage = auditLogRepository.findAll(spec, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", logPage.getContent());
        response.put("currentPage", logPage.getNumber());
        response.put("totalElements", logPage.getTotalElements());
        response.put("totalPages", logPage.getTotalPages());
        response.put("pageSize", logPage.getSize());

        return ResponseEntity.ok(response);
    }
}
