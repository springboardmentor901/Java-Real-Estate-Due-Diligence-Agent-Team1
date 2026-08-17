package com.realestate.due_diligence_agent.property;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String address;

    private String propertyType;

    private Integer bedrooms;

    private Integer bathrooms;

    private Double squareFeet;

    private Integer yearBuilt;
}