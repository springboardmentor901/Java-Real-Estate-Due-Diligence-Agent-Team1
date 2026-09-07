package com.realestate.due_diligence_agent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "ownership_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnershipRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "ownership_type")
    private String ownershipType;

    @Column(name = "acquisition_date")
    private LocalDate acquisitionDate;

    @Column(name = "payment_status")
    private String paymentStatus;
}