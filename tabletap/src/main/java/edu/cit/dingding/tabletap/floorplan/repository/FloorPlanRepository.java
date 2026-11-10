package edu.cit.dingding.tabletap.floorplan.repository;

import edu.cit.dingding.tabletap.floorplan.model.FloorPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// This repository provides database access for floor plans.
public interface FloorPlanRepository extends JpaRepository<FloorPlan, UUID> {
}
