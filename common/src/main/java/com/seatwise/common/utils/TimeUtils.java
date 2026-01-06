package com.seatwise.common.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for timezone conversions.
 * Converts UTC Instant to user's timezone or falls back to UTC.
 */
public class TimeUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Convert UTC Instant to user timezone if provided, fallback to UTC.
     * Returns formatted string in pattern: yyyy-MM-dd HH:mm:ss
     *
     * @param utcInstant the UTC instant to convert
     * @param userTimeZone the user's timezone (e.g., "America/New_York", "Europe/London")
     * @return formatted date string in user timezone or UTC, or null if instant is null
     */
    public static String toUserOrUtc(Instant utcInstant, String userTimeZone) {
        if (utcInstant == null) return null;

        ZoneId zoneId;
        try {
            // if header is missing or invalid, fallback to UTC
            zoneId = (userTimeZone != null && !userTimeZone.isBlank())
                    ? ZoneId.of(userTimeZone)
                    : ZoneId.of("UTC");
        } catch (Exception e) {
            zoneId = ZoneId.of("UTC"); // fallback if invalid timezone
        }

        ZonedDateTime zonedDateTime = utcInstant.atZone(zoneId);
        return zonedDateTime.format(FORMATTER);
    }

    /**
     * Return ZonedDateTime directly in user timezone or UTC.
     *
     * @param utcInstant the UTC instant to convert
     * @param userTimeZone the user's timezone (e.g., "America/New_York", "Europe/London")
     * @return ZonedDateTime in user timezone or UTC, or null if instant is null
     */
    public static ZonedDateTime toUserOrUtcZoned(Instant utcInstant, String userTimeZone) {
        if (utcInstant == null) return null;

        ZoneId zoneId;
        try {
            zoneId = (userTimeZone != null && !userTimeZone.isBlank())
                    ? ZoneId.of(userTimeZone)
                    : ZoneId.of("UTC");
        } catch (Exception e) {
            zoneId = ZoneId.of("UTC");
        }

        return utcInstant.atZone(zoneId);
    }
}

