package com.example.demo.payloads;

import lombok.Data;

@Data
public class EmailRequest {
    private String to;
    private String subject;
    private String body;

    // getters and setters
}

