package edu.cit.dingding.tabletap.floorplan.service;

import edu.cit.dingding.tabletap.floorplan.dto.TableRequest;
import edu.cit.dingding.tabletap.floorplan.model.FloorPlan;
import edu.cit.dingding.tabletap.floorplan.model.RestaurantTable;
import edu.cit.dingding.tabletap.floorplan.repository.RestaurantTableRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

// This service manages restaurant tables without hard-deleting them.
@Service
public class RestaurantTableService {

    private final RestaurantTableRepository restaurantTableRepository;
    private final FloorPlanService floorPlanService;

    public RestaurantTableService(
            RestaurantTableRepository restaurantTableRepository,
            FloorPlanService floorPlanService
    ) {
        this.restaurantTableRepository = restaurantTableRepository;
        this.floorPlanService = floorPlanService;
    }

    public RestaurantTable create(TableRequest request) {
        RestaurantTable table = new RestaurantTable();
        updateFields(table, request);
        return restaurantTableRepository.save(table);
    }

    public List<RestaurantTable> findAllActive() {
        return restaurantTableRepository.findByActiveTrue();
    }

    public List<RestaurantTable> findByFloorPlan(UUID floorPlanId) {
        return restaurantTableRepository.findByFloorPlanFloorPlanIdAndActiveTrue(floorPlanId);
    }

    public RestaurantTable findById(UUID id) {
        return restaurantTableRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found"));
    }

    public RestaurantTable update(UUID id, TableRequest request) {
        RestaurantTable table = findById(id);
        updateFields(table, request);
        return restaurantTableRepository.save(table);
    }

    public void deactivate(UUID id) {
        RestaurantTable table = findById(id);
        table.setActive(false);
        restaurantTableRepository.save(table);
    }

    private void updateFields(RestaurantTable table, TableRequest request) {
        FloorPlan floorPlan = floorPlanService.findById(request.floorPlanId());
        table.setFloorPlan(floorPlan);
        table.setTableNumber(request.tableNumber());
        table.setCapacity(request.capacity());
        table.setPositionX(request.positionX());
        table.setPositionY(request.positionY());
    }
}
