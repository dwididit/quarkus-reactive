package dev.dwidi.utils;

import dev.dwidi.dto.BaseResponseDTO;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;

public class DateValidator {
    private static final LocalDate MIN_DATE = LocalDate.of(1995, 6, 16);
    private static final LocalDate MAX_DATE = LocalDate.now();
    private static final int MAX_DATE_RANGE_DAYS = 30;

    private DateValidator() {
        throw new UnsupportedOperationException("This is util class");
    }

    public static <T> Uni<BaseResponseDTO<T>> validateDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            validateDateBounds(startDate, "Start date");
            validateDateBounds(endDate, "End date");
            validateDateOrder(startDate, endDate);
            validateDateRangeLimit(startDate, endDate);

            return Uni.createFrom().nullItem();
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(new BaseResponseDTO<>(
                    Response.Status.BAD_REQUEST.getStatusCode(),
                    e.getMessage(),
                    null
            ));
        }
    }

    private static void validateDateBounds(LocalDate date, String fieldName) {
        if (date == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
        if (date.isBefore(MIN_DATE)) {
            throw new IllegalArgumentException(fieldName + " cannot be before " + MIN_DATE);
        }
        if (date.isAfter(MAX_DATE)) {
            throw new IllegalArgumentException(fieldName + " cannot be after " + MAX_DATE);
        }
    }

    private static void validateDateOrder(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after or equal to start date");
        }
    }

    private static void validateDateRangeLimit(LocalDate startDate, LocalDate endDate) {
        long daysBetween = endDate.toEpochDay() - startDate.toEpochDay();
        if (daysBetween > MAX_DATE_RANGE_DAYS) {
            throw new IllegalArgumentException("Date range cannot exceed " + MAX_DATE_RANGE_DAYS + " days");
        }
    }
}