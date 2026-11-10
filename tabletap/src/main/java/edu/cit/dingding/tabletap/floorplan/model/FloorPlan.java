package edu.cit.dingding.tabletap.floorplan.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

// This entity stores a restaurant floor plan.
@Entity
@Table(name = "floor_plans")
@Getter
@Setter
@NoArgsConstructor
public class FloorPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID floorPlanId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String layoutData;

    private LocalDateTime createdAt = LocalDateTime.now();
}
