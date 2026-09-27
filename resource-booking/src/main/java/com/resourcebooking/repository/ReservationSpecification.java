package com.resourcebooking.repository;

import com.resourcebooking.entity.Reservation;
import com.resourcebooking.enums.ReservationStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

// small helper to build the where clause based on whatever filters were passed
// (learned this pattern from spring docs, saves writing a dozen findBy methods)
public class ReservationSpecification {

    public static Specification<Reservation> withFilters(Long userId, ReservationStatus status,
                                                           BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            // if userId is passed we only want that user's reservations (used for normal USER role)
            if (userId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("user").get("id"), userId));
            }

            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }

            if (minPrice != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return predicates;
        };
    }
}
