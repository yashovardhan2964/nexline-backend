package com.yash.smartqueue.repository;

import com.yash.smartqueue.model.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceTypeRepository extends JpaRepository<ServiceType, Long> {
}