package com.resourcebooking.repository;

import com.resourcebooking.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

// JpaSpecificationExecutor lets us build the filter query dynamically
// (status / minPrice / maxPrice are all optional so a normal findBy... method won't work well)
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    // used to block double-booking - finds any PENDING/CONFIRMED reservation on the same
    // resource whose time range overlaps the one being requested. CANCELLED ones don't count.
    @Query("SELECT r FROM Reservation r WHERE r.resource.id = :resourceId " +
            "AND r.status <> com.resourcebooking.enums.ReservationStatus.CANCELLED " +
            "AND r.startTime < :endTime AND r.endTime > :startTime")
    List<Reservation> findOverlapping(@Param("resourceId") Long resourceId,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);
}
