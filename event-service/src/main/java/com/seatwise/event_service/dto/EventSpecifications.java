package com.seatwise.event_service.dto;

import com.seatwise.event_service.model.Event;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class EventSpecifications {

    public static Specification<Event> textSearch(String query) {
        return (root, queryBuilder, cb) -> {
            if (query == null) return null; // no condition
            // Full-text search (or use LIKE for simpler cases)
            return cb.or(
                    cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("title")), "%" + query.toLowerCase() + "%")
            );
        };
    }
    public static Specification<Event> venueNameContains(String venueName) {
        return (root, q, cb) -> {
            if (venueName == null || venueName.trim().isEmpty()) return null;
            String pattern = "%" + venueName.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("venue").get("name")), pattern);
        };
    }

    public static Specification<Event> timeBetween(Instant from, Instant to) {
        return (root, q, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null) return cb.between(root.get("time"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("time"), from);
            return cb.lessThanOrEqualTo(root.get("time"), to);
        };
    }

    public static Specification<Event> minAvailableSeats(Integer min) {
        return (root, q, cb) ->
                min == null ? null : cb.greaterThanOrEqualTo(root.get("availableSeats"), min);
    }
}