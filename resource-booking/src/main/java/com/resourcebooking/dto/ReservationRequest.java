package com.resourcebooking.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// note: no userId field here on purpose - user comes from the JWT, not from what
// the client sends. otherwise anyone could book on behalf of someone else
@Getter
@Setter
public class ReservationRequest {

    @NotNull(message = "resourceId is required")
    private Long resourceId;

    @NotNull(message = "startTime is required")
    private LocalDateTime startTime;

    @NotNull(message = "endTime is required")
    private LocalDateTime endTime;

    @NotNull(message = "price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "price can't be negative")
    private BigDecimal price;

    // cross-field check - bean validation runs this automatically because of the "is" prefix.
    // skips the check if either field is null since @NotNull above already flags that separately
    @AssertTrue(message = "endTime must be after startTime")
    public boolean isEndTimeAfterStartTime() {
        if (startTime == null || endTime == null) {
            return true;
        }
        return endTime.isAfter(startTime);
    }
}
