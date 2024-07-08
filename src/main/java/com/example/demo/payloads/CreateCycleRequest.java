package com.example.demo.payloads;

import lombok.Data;

@Data
public class CreateCycleRequest {
    private String cycleNumber;
    private String available;
}
