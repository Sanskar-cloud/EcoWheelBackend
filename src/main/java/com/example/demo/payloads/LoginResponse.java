package com.example.demo.payloads;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;

    private UserDto user;

}
