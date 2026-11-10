package edu.cit.dingding.tabletap.floorplan.dto;

import jakarta.validation.constraints.NotBlank;

// This request carries the name and layout for a floor plan.
public record FloorPlanRequest(
        @NotBlank String name,
        String layoutData
) {
}
