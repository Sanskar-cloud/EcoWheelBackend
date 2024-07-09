package com.example.demo.repository;

import com.example.demo.entities.Subscription;
import com.example.demo.entities.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepo extends JpaRepository<Subscription,Integer> {
    Optional<Subscription> findByPaymentLinkId(String paymentLinkId);
    List<Subscription> findAllByEndDateBeforeAndStatus(LocalDate endDate, SubscriptionStatus status);
    @Query("SELECT s FROM Subscription s WHERE s.status = :status ")
    List<Subscription> findAllExpiredSubscriptions(@Param("status") SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.endDate = :fourDaysAhead")
    List<Subscription> findAllSubscriptionsEndingInFourDays(LocalDate fourDaysAhead);
}
