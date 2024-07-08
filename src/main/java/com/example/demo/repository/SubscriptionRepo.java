package com.example.demo.repository;

import com.example.demo.entities.Subscription;
import com.example.demo.entities.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepo extends JpaRepository<Subscription,Integer> {
    Optional<Subscription> findByPaymentLinkId(String paymentLinkId);
    List<Subscription> findAllByEndDateBeforeAndStatus(LocalDate endDate, SubscriptionStatus status);
}
