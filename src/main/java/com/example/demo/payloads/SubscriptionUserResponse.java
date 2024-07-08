package com.example.demo.payloads;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SubscriptionUserResponse {
    private Long subscriptionId;
    private String userName;
    private String userEmail;
    private String phoneNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String subscriptionPlanName;
    private String subscriptionStatus;
}

