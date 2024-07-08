package com.example.demo.services;

import com.example.demo.payloads.*;

import java.util.List;

public interface CycleService  {
    CycleDto addCycle(CreateCycleRequest createCycleRequest);






    CycleDto getCycleById(Integer cycleId);

    CycleResponse getAllCycle(Integer pageNumber, Integer pageSize, String sortBy, String sortDir);

    void deleteCycle(Integer cycleId);
    List<CycleDto> getCyclesByUser(Integer userId);
    List<CycleDto> searchCycle(String keyword);
}
