package edu.cit.dingding.tabletap.floorplan.controller;

import edu.cit.dingding.tabletap.floorplan.dto.FloorPlanRequest;
import edu.cit.dingding.tabletap.floorplan.model.FloorPlan;
import edu.cit.dingding.tabletap.floorplan.service.FloorPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// This controller provides the API for managing floor plans.
@RestController
@RequestMapping("/api/floor-plans")
public class FloorPlanController {

    private final FloorPlanService floorPlanService;

    public FloorPlanController(FloorPlanService floorPlanService) {
        this.floorPlanService = floorPlanService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FloorPlan create(@Valid @RequestBody FloorPlanRequest request) {
        return floorPlanService.create(request);
    }

    @GetMapping
    public List<FloorPlan> findAll() {
        return floorPlanService.findAll();
    }

    @GetMapping("/{id}")
    public FloorPlan findById(@PathVariable UUID id) {
        return floorPlanService.findById(id);
    }

    @PutMapping("/{id}")
    public FloorPlan update(@PathVariable UUID id, @Valid @RequestBody FloorPlanRequest request) {
        return floorPlanService.update(id, request);
    }
}
