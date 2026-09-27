package com.resourcebooking.dto;

import com.resourcebooking.entity.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResourceResponse {
    private Long id;
    private String name;
    private String type;
    private String description;
    private String location;
    private boolean available;

    public static ResourceResponse fromEntity(Resource r) {
        return new ResourceResponse(r.getId(), r.getName(), r.getType(), r.getDescription(),
                r.getLocation(), r.isAvailable());
    }
}
