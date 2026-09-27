package com.resourcebooking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Resource = the thing that can be booked (room, vehicle, equipment etc)
@Entity
@Table(name = "resources")
@Getter
@Setter
@NoArgsConstructor
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    // kept as plain string instead of enum, easier to add new types later
    private String type;

    @Column(length = 1000)
    private String description;

    private String location;

    private boolean available = true;

    public Resource(String name, String type, String description, String location, boolean available) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.location = location;
        this.available = available;
    }
}
