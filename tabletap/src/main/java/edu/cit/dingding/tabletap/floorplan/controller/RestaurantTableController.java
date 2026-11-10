package edu.cit.dingding.tabletap.floorplan.controller;

import edu.cit.dingding.tabletap.floorplan.dto.TableRequest;
import edu.cit.dingding.tabletap.floorplan.model.RestaurantTable;
import edu.cit.dingding.tabletap.floorplan.service.RestaurantTableService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// This controller provides the API for managing restaurant tables.
@RestController
@RequestMapping("/api/tables")
public class RestaurantTableController {

    private final RestaurantTableService restaurantTableService;

    public RestaurantTableController(RestaurantTableService restaurantTableService) {
        this.restaurantTableService = restaurantTableService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantTable create(@Valid @RequestBody TableRequest request) {
        return restaurantTableService.create(request);
    }

    @GetMapping
    public List<RestaurantTable> findActive(@RequestParam(required = false) UUID floorPlanId) {
        if (floorPlanId != null) {
            return restaurantTableService.findByFloorPlan(floorPlanId);
        }
        return restaurantTableService.findAllActive();
    }

    @GetMapping("/{id}")
    public RestaurantTable findById(@PathVariable UUID id) {
        return restaurantTableService.findById(id);
    }

    @PutMapping("/{id}")
    public RestaurantTable update(@PathVariable UUID id, @Valid @RequestBody TableRequest request) {
        return restaurantTableService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        restaurantTableService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
