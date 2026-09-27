package com.resourcebooking.controller;

import com.resourcebooking.dto.ReservationRequest;
import com.resourcebooking.dto.ReservationResponse;
import com.resourcebooking.dto.ReservationStatusUpdateRequest;
import com.resourcebooking.entity.User;
import com.resourcebooking.enums.ReservationStatus;
import com.resourcebooking.enums.Role;
import com.resourcebooking.service.ReservationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Create and manage reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // any logged in user can book, identity comes from the token (@AuthenticationPrincipal)
    // not from anything in the request body
    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request,
                                                        @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.create(request, currentUser));
    }

    // GET /api/reservations?status=PENDING&minPrice=10&maxPrice=100&page=0&size=10&sortBy=createdAt&sortDir=desc
    @GetMapping
    public ResponseEntity<Page<ReservationResponse>> getAll(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        boolean isAdmin = isAdmin(currentUser);

        return ResponseEntity.ok(reservationService.getReservations(
                currentUser, isAdmin, status, minPrice, maxPrice, page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getById(@PathVariable Long id,
                                                        @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(reservationService.getById(id, currentUser, isAdmin(currentUser)));
    }

    // admin only - approve/confirm/cancel a reservation
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationResponse> updateStatus(@PathVariable Long id,
                                                             @Valid @RequestBody ReservationStatusUpdateRequest request) {
        return ResponseEntity.ok(reservationService.updateStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        reservationService.delete(id, currentUser, isAdmin(currentUser));
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(User user) {
        // security config already blocks unauthenticated requests from reaching here, but
        // checking null anyway rather than trusting that path always holds
        return user != null && user.getRole() == Role.ADMIN;
    }
}
