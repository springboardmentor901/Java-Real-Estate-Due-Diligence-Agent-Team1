package com.realestate.due_diligence_agent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "flood_zone_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FloodZoneData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "flood_zone")
    private String floodZone;

    @Column(name = "flood_risk_rating")
    private String floodRiskRating;

    @Column(name = "fema_map_panel")
    private String femaMapPanel;

    @Column(name = "elevation_data")
    private String elevationData;

    @Column(name = "retrieved_at")
    private LocalDateTime retrievedAt;

    @PrePersist
    protected void onCreate() {
        retrievedAt = LocalDateTime.now();
    }
}
