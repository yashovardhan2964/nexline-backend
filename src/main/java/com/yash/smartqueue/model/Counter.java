package com.yash.smartqueue.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "counters")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Counter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // e.g. "Counter 1"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private CounterStatus status = CounterStatus.OFFLINE;

    // CAPABILITY: which service types this counter is allowed to serve
    @ManyToMany
    @JoinTable(
            name = "counter_service_types",
            joinColumns = @JoinColumn(name = "counter_id"),
            inverseJoinColumns = @JoinColumn(name = "service_type_id")
    )
    private Set<ServiceType> capableServiceTypes = new HashSet<>();

    // CURRENT ASSIGNMENT: which service type it's actively serving right now
    @ManyToOne
    @JoinColumn(name = "current_service_type_id")
    private ServiceType currentServiceType;
}