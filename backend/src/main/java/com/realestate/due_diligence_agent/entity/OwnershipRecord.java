package com.realestate.due_diligence_agent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "ownership_type")
    private String ownershipType;

    @Column(name = "acquisition_date")
    private LocalDate acquisitionDate;

    private String source;

    @Column(name = "retrieved_at")
    private LocalDateTime retrievedAt;

    @PrePersist
    protected void onCreate() {
        retrievedAt = LocalDateTime.now();
    }
}
