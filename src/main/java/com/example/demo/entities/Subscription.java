package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
@Data
@Entity
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    private User user;

    @ManyToOne
    private SubscriptionPlan plan;

    private LocalDate startDate;
    private LocalDate endDate;
    private String returnStatus;
    @OneToOne

    private Cycle allocatedCycle;
    private String paymentLinkId;
    private SubscriptionStatus status;



    // getters and setters
}

