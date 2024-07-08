package com.example.demo.payloads;

import lombok.Data;

@Data
public class PaymentRequest {
    String userId;
    String userName;
    String userEmail;
    SubscriptionDto subscriptionDto;
}
