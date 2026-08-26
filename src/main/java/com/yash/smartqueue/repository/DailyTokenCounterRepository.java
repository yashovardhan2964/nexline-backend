package com.yash.smartqueue.repository;

import com.yash.smartqueue.model.DailyTokenCounter;
import com.yash.smartqueue.model.ServiceType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyTokenCounterRepository extends JpaRepository<DailyTokenCounter, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DailyTokenCounter d WHERE d.serviceType = :serviceType AND d.counterDate = :date")
    Optional<DailyTokenCounter> findForUpdate(@Param("serviceType") ServiceType serviceType,
                                              @Param("date") LocalDate date);
}
