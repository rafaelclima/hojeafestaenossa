package com.rafaellima.hojeafestaenossa.event.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rafaellima.hojeafestaenossa.event.domain.Event;

public interface EventRepository extends JpaRepository<Event, UUID> {

    Optional<Event> findByAccessToken(String accessToken);

    Page<Event> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.startedAt <= :now AND e.expiredAt >= :now")
    long countActive(@Param("now") Instant now);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.expiredAt < :now")
    long countExpired(@Param("now") Instant now);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.startedAt > :now")
    long countUpcoming(@Param("now") Instant now);

    @Query(value = "SELECT TO_CHAR(created_at, 'YYYY-MM') as month, COUNT(*) as count " +
           "FROM events " +
           "WHERE created_at >= :startDate " +
           "GROUP BY TO_CHAR(created_at, 'YYYY-MM') " +
           "ORDER BY month", nativeQuery = true)
    java.util.List<Object[]> countEventsByMonth(@Param("startDate") Instant startDate);

}
