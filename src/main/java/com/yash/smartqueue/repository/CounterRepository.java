package com.yash.smartqueue.repository;

import com.yash.smartqueue.model.Counter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounterRepository extends JpaRepository<Counter, Long> {
}