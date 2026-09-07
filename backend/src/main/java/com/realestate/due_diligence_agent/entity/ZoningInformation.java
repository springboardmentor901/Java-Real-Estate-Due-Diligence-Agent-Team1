package com.realestate.due_diligence_agent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "zoning_information")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoningInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "zoning_classification")
    private String zoningClassification;

    @Column(name = "land_use")
    private String landUse;

    @Column(name = "setback_requirements")
    private String setbackRequirements;

    @Column(name = "zoning_compliance")
    private Boolean zoningCompliance;

    private String source;

    @Column(name = "retrieved_at")
    private LocalDateTime retrievedAt;

    @PrePersist
    protected void onCreate() {
        retrievedAt = LocalDateTime.now();
    }
}
