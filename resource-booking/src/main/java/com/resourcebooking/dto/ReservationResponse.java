package com.resourcebooking.dto;

import com.resourcebooking.entity.Reservation;
import com.resourcebooking.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReservationResponse {
    private Long id;
    private Long resourceId;
    private String resourceName;
    private Long userId;
    private String username;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;
    private BigDecimal price;
    private LocalDateTime createdAt;

    public static ReservationResponse fromEntity(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getResource().getId(),
                r.getResource().getName(),
                r.getUser().getId(),
                r.getUser().getUsername(),
                r.getStartTime(),
                r.getEndTime(),
                r.getStatus(),
                r.getPrice(),
                r.getCreatedAt()
        );
    }
}
