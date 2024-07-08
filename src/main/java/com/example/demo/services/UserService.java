package com.example.demo.services;

import com.example.demo.payloads.CycleResponse;
import com.example.demo.payloads.RegisterRequest;
import com.example.demo.payloads.UserDto;
import com.example.demo.payloads.UserResponse;

import java.util.List;

public interface UserService {
    UserDto registerNewUser(RegisterRequest registerRequest);


    UserDto createUser(UserDto user);

    UserDto updateUser(UserDto user, Integer userId);

    UserDto getUserById(Integer userId);

   UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortDir);

    void deleteUser(Integer userId);
}
