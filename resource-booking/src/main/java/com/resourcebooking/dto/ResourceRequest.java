package com.resourcebooking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// used for both create and update, didn't feel like making 2 separate classes
@Getter
@Setter
public class ResourceRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "type is required")
    private String type;

    private String description;

    @NotBlank(message = "location is required")
    private String location;

    private boolean available = true;
}
