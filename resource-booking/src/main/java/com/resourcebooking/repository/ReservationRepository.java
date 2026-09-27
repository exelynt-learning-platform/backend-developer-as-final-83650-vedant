package com.resourcebooking.repository;

import com.resourcebooking.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

// JpaSpecificationExecutor lets us build the filter query dynamically
// (status / minPrice / maxPrice are all optional so a normal findBy... method won't work well)
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {
}
