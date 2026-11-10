package edu.cit.dingding.tabletap.floorplan.service;

import edu.cit.dingding.tabletap.floorplan.dto.FloorPlanRequest;
import edu.cit.dingding.tabletap.floorplan.model.FloorPlan;
import edu.cit.dingding.tabletap.floorplan.repository.FloorPlanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

// This service creates and retrieves floor plans.
@Service
public class FloorPlanService {

    private final FloorPlanRepository floorPlanRepository;

    public FloorPlanService(FloorPlanRepository floorPlanRepository) {
        this.floorPlanRepository = floorPlanRepository;
    }

    public FloorPlan create(FloorPlanRequest request) {
        FloorPlan floorPlan = new FloorPlan();
        floorPlan.setName(request.name());
        floorPlan.setLayoutData(request.layoutData());
        return floorPlanRepository.save(floorPlan);
    }

    public List<FloorPlan> findAll() {
        return floorPlanRepository.findAll();
    }

    public FloorPlan findById(UUID id) {
        return floorPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Floor plan not found"));
    }

    public FloorPlan update(UUID id, FloorPlanRequest request) {
        FloorPlan floorPlan = findById(id);
        floorPlan.setName(request.name());
        floorPlan.setLayoutData(request.layoutData());
        return floorPlanRepository.save(floorPlan);
    }
}
