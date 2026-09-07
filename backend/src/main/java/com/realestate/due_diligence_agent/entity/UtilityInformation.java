package com.realestate.due_diligence_agent.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "utility_information")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilityInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "utility_type")
    private String utilityType;

    private String provider;
    private String status;
    private String source;
}
