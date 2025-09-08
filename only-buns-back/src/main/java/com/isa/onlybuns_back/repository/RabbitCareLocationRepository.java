package com.isa.onlybuns_back.repository;

import com.isa.onlybuns_back.model.RabbitCareLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RabbitCareLocationRepository extends JpaRepository<RabbitCareLocation, Long> {
}