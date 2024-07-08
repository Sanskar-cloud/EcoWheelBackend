package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Cycle {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String cycleNumber;
    private String isAvailable;

    @ManyToOne
    private User user;

    // getters and setters
}

