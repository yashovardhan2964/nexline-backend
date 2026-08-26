package com.yash.smartqueue.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "daily_token_counters",
        uniqueConstraints = @UniqueConstraint(columnNames = {"service_type_id", "counter_date"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyTokenCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "service_type_id", nullable = false)
    private ServiceType serviceType;

    @Column(name = "counter_date", nullable = false)
    private LocalDate counterDate;

    @Column(nullable = false)
    private Integer lastNumber = 0;
}