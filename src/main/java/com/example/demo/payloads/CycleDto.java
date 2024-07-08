package com.example.demo.payloads;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class CycleDto {

    private int id;
    private String cycleNumber;
    private String available;
    private UserDto user;
}
