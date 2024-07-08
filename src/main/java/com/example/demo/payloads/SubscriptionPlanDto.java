package com.example.demo.payloads;

import lombok.Data;

@Data
public class SubscriptionPlanDto {
    private int id;
    private String name;
    private double price;
    private int duration;
}