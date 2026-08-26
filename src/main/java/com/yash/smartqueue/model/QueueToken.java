package com.yash.smartqueue.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "queue_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer tokenNumber;

    // Guest customer identity — no User account required
    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerPhone;

    @ManyToOne
    @JoinColumn(name = "service_type_id", nullable = false)
    private ServiceType serviceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private Priority priority = Priority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private TokenStatus status = TokenStatus.WAITING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime calledAt;

    private LocalDateTime completedAt;

    @ManyToOne
    @JoinColumn(name = "counter_id")
    private Counter counter; // nullable — unassigned until called
}