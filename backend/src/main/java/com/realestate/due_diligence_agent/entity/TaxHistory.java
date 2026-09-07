package com.realestate.due_diligence_agent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tax_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "tax_year")
    private Integer taxYear;

    @Column(name = "assessed_value")
    private BigDecimal assessedValue;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount;

    @Column(name = "payment_status")
    private String paymentStatus;

    private String source;

    @Column(name = "retrieved_at")
    private LocalDateTime retrievedAt;

    @PrePersist
    protected void onCreate() {
        retrievedAt = LocalDateTime.now();
    }
}
