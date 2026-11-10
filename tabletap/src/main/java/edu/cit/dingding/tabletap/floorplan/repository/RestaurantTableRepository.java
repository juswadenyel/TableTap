package edu.cit.dingding.tabletap.floorplan.repository;

import edu.cit.dingding.tabletap.floorplan.model.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// This repository provides database access for restaurant tables.
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, UUID> {

    List<RestaurantTable> findByActiveTrue();

    List<RestaurantTable> findByFloorPlanFloorPlanIdAndActiveTrue(UUID floorPlanId);
}
