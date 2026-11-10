package edu.cit.dingding.tabletap.floorplan.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// This request carries the details needed to create or update a table.
public record TableRequest(
        @NotNull UUID floorPlanId,
        @NotBlank String tableNumber,
        @Min(1) int capacity,
        int positionX,
        int positionY
) {
}
