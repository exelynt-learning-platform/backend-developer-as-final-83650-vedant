package com.resourcebooking.service;

import com.resourcebooking.dto.ResourceRequest;
import com.resourcebooking.dto.ResourceResponse;
import com.resourcebooking.entity.Resource;
import com.resourcebooking.exception.ResourceNotFoundException;
import com.resourcebooking.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public List<ResourceResponse> getAll() {
        return resourceRepository.findAll()
                .stream()
                .map(ResourceResponse::fromEntity)
                .toList();
    }

    public ResourceResponse getById(Long id) {
        Resource resource = findOrThrow(id);
        return ResourceResponse.fromEntity(resource);
    }

    // admin only - enforced at controller/security level, not here
    public ResourceResponse create(ResourceRequest request) {
        Resource resource = new Resource(
                request.getName(),
                request.getType(),
                request.getDescription(),
                request.getLocation(),
                request.isAvailable()
        );
        return ResourceResponse.fromEntity(resourceRepository.save(resource));
    }

    public ResourceResponse update(Long id, ResourceRequest request) {
        Resource resource = findOrThrow(id);
        resource.setName(request.getName());
        resource.setType(request.getType());
        resource.setDescription(request.getDescription());
        resource.setLocation(request.getLocation());
        resource.setAvailable(request.isAvailable());
        return ResourceResponse.fromEntity(resourceRepository.save(resource));
    }

    public void delete(Long id) {
        Resource resource = findOrThrow(id);
        resourceRepository.delete(resource);
    }

    // used internally by ReservationService too, so returning the entity not the dto
    public Resource findOrThrow(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id " + id));
    }
}
