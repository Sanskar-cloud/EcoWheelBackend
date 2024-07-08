package com.example.demo.repository;

import com.example.demo.entities.Cycle;
import com.example.demo.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CycleRepo extends JpaRepository<Cycle,Integer> {
    List<Cycle> findByUser(User user);
    @Query("select c from Cycle c where c.cycleNumber like :key")
    List<Cycle> searchByTitle(@Param("key") String title);
    Optional<Cycle> findFirstByIsAvailableTrue();
    List<Cycle> findByIsAvailableTrueOrderByCycleNumberAsc();
}
