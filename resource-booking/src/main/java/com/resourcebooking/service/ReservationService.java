package com.resourcebooking.service;

import com.resourcebooking.dto.ReservationRequest;
import com.resourcebooking.dto.ReservationResponse;
import com.resourcebooking.entity.Reservation;
import com.resourcebooking.entity.Resource;
import com.resourcebooking.entity.User;
import com.resourcebooking.enums.ReservationStatus;
import com.resourcebooking.exception.AccessDeniedCustomException;
import com.resourcebooking.exception.ConflictException;
import com.resourcebooking.exception.ResourceNotFoundException;
import com.resourcebooking.repository.ReservationRepository;
import com.resourcebooking.repository.ReservationSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@Service
public class ReservationService {

    // only these columns are allowed in ?sortBy= - anything else would either blow up with a
    // PropertyReferenceException (500) or let someone probe the entity's field names
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt", "startTime", "endTime", "price", "status"
    );

    private final ReservationRepository reservationRepository;
    private final ResourceService resourceService;

    public ReservationService(ReservationRepository reservationRepository, ResourceService resourceService) {
        this.reservationRepository = reservationRepository;
        this.resourceService = resourceService;
    }

    // creates a reservation for whoever is currently logged in (currentUser comes from the JWT
    // in the controller, never trust an id sent in the request body)
    public ReservationResponse create(ReservationRequest request, User currentUser) {
        Resource resource = resourceService.findOrThrow(request.getResourceId());

        // block double-booking - is there already a non-cancelled reservation on this
        // resource that overlaps the requested time range?
        boolean overlaps = !reservationRepository.findOverlapping(
                request.getResourceId(), request.getStartTime(), request.getEndTime()
        ).isEmpty();

        if (overlaps) {
            // 409, not 400 - the request itself is well-formed, it just conflicts with an
            // existing booking
            throw new ConflictException("This resource is already booked for the requested time slot");
        }

        Reservation reservation = new Reservation();
        reservation.setResource(resource);
        reservation.setUser(currentUser);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setPrice(request.getPrice());
        reservation.setStatus(ReservationStatus.PENDING);

        return ReservationResponse.fromEntity(reservationRepository.save(reservation));
    }

    // main listing endpoint - admin sees everything, normal user only sees their own
    // (isAdmin decided in the controller based on the role in the JWT)
    public Page<ReservationResponse> getReservations(User currentUser, boolean isAdmin,
                                                       ReservationStatus status, BigDecimal minPrice,
                                                       BigDecimal maxPrice, int page, int size,
                                                       String sortBy, String sortDir) {

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "sortBy must be one of " + ALLOWED_SORT_FIELDS + " (got '" + sortBy + "')");
        }

        if (!sortDir.equalsIgnoreCase("asc") && !sortDir.equalsIgnoreCase("desc")) {
            throw new IllegalArgumentException("sortDir must be 'asc' or 'desc' (got '" + sortDir + "')");
        }

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Long userIdFilter = isAdmin ? null : currentUser.getId();

        var spec = ReservationSpecification.withFilters(userIdFilter, status, minPrice, maxPrice);

        return reservationRepository.findAll(spec, pageable)
                .map(ReservationResponse::fromEntity);
    }

    public ReservationResponse getById(Long id, User currentUser, boolean isAdmin) {
        Reservation reservation = findOrThrow(id);

        // a normal user can only look at their own reservation
        if (!isAdmin && !reservation.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedCustomException("You don't have permission to view this reservation");
        }

        return ReservationResponse.fromEntity(reservation);
    }

    // admin only, updates the status (approve/confirm/cancel etc)
    public ReservationResponse updateStatus(Long id, ReservationStatus status) {
        Reservation reservation = findOrThrow(id);
        reservation.setStatus(status);
        return ReservationResponse.fromEntity(reservationRepository.save(reservation));
    }

    public void delete(Long id, User currentUser, boolean isAdmin) {
        Reservation reservation = findOrThrow(id);

        if (!isAdmin && !reservation.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedCustomException("You don't have permission to delete this reservation");
        }

        reservationRepository.delete(reservation);
    }

    private Reservation findOrThrow(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id " + id));
    }
}
