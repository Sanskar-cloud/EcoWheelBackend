package com.example.demo.payloads;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SubscriptionDto {
    private int id;
    private LocalDate startDate;
    private LocalDate endDate;
    private SubscriptionPlanDto plan;
    private List<CycleDto> cycles;
}
