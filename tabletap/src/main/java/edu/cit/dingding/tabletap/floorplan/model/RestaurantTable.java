package edu.cit.dingding.tabletap.floorplan.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

// This entity stores a table and its position on a floor plan.
@Entity
@Table(name = "restaurant_tables")
@Getter
@Setter
@NoArgsConstructor
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID tableId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "floor_plan_id")
    private FloorPlan floorPlan;

    @Column(nullable = false)
    private String tableNumber;

    @Column(nullable = false)
    private int capacity;

    private int positionX;

    private int positionY;

    private boolean active = true;
}
