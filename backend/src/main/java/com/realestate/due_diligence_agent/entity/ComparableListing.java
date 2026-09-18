package com.realestate.due_diligence_agent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comparable_listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComparableListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comparable_address", nullable = false)
    private String comparableAddress;

    @Column(nullable = false)
    private Double price;
    @Column(name = "square_feet")
private Double squareFeet;

    @Column(name = "distance_miles")
    private Double distanceMiles;

    @Column(name = "listed_date")
    private LocalDateTime listedDate;

    @Column
    private String source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;
}