package com.realestate.due_diligence_agent.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false, unique = true)
    private Property property;

    @Column(name = "zoning_code")
    private String zoningCode;

    @Column(name = "zoning_description")
    private String zoningDescription;

    @Column(name = "zoning_classification")
    private String zoningClassification;

    @Column(name = "land_use")
    private String landUse;

    @Column(name = "setback_requirements")
    private String setbackRequirements;

    @Column(name = "zoning_compliance")
    private Boolean zoningCompliance;

    @Column(name = "retrieved_at")
    private LocalDateTime retrievedAt;
}