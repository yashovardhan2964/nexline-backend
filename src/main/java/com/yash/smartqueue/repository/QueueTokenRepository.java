package com.yash.smartqueue.repository;

import com.yash.smartqueue.model.QueueToken;
import com.yash.smartqueue.model.ServiceType;
import com.yash.smartqueue.model.TokenStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QueueTokenRepository extends JpaRepository<QueueToken, Long> {

    // For generating the next tokenNumber per ServiceType per day
    Optional<QueueToken> findFirstByServiceTypeAndCreatedAtAfterOrderByTokenNumberDesc(
            ServiceType serviceType, LocalDateTime startOfDay);

    // For live queue tracking - waiting tokens for a service type, oldest first
    List<QueueToken> findByServiceTypeAndStatusOrderByCreatedAtAsc(
            ServiceType serviceType, TokenStatus status);

    // For "Next Token" - lock all WAITING tokens for this service type so concurrent
    // admin actions can't pick the same token
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM QueueToken t WHERE t.serviceType = :serviceType AND t.status = 'WAITING'")
    List<QueueToken> findWaitingTokensForUpdate(@Param("serviceType") ServiceType serviceType);
}