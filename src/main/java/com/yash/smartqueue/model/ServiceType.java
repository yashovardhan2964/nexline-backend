package com.yash.smartqueue.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "service_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g. "Billing", "Consultation"

    @Column(nullable = false, unique = true, length = 5)
    private String prefix; // e.g. "B", "C", "XR" - used in display token like "B-14"

    @Column(name = "avg_service_minutes")
    private Double avgServiceMinutes; // default/seed estimate, AI will refine this later
}