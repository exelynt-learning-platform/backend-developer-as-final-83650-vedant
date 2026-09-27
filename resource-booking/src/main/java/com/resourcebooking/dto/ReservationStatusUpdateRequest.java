package com.resourcebooking.dto;

import com.resourcebooking.enums.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// small dto just for the admin endpoint that changes a reservation's status
@Getter
@Setter
public class ReservationStatusUpdateRequest {

    @NotNull(message = "status is required")
    private ReservationStatus status;
}
