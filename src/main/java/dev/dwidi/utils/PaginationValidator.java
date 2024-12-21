package dev.dwidi.utils;

import jakarta.ws.rs.BadRequestException;

public class PaginationValidator {
    private PaginationValidator() {
        throw new UnsupportedOperationException("This is util class");
    }

    public static void validatePaginationParams(int page, int size, String sortBy, String sortDirection) {
        validatePage(page);
        validateSize(size);
        validateSortDirection(sortDirection);
        validateSortBy(sortBy);
    }

    public static void validatePage(int page) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be negative");
        }
    }

    public static void validateSize(int size) {
        if (size <= 0) {
            throw new BadRequestException("Page size must be greater than 0");
        }
        if (size > 100) {
            throw new BadRequestException("Page size cannot exceed 100");
        }
    }

    public static void validateSortDirection(String sortDirection) {
        if (sortDirection == null || sortDirection.isBlank()) {
            throw new BadRequestException("Sort direction cannot be empty");
        }
        if (!sortDirection.equalsIgnoreCase("asc") && !sortDirection.equalsIgnoreCase("desc")) {
            throw new BadRequestException("Sort direction must be either 'asc' or 'desc'");
        }
    }

    public static void validateSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            throw new BadRequestException("Sort field cannot be empty");
        }
        // Add valid fields for sorting
        if (!sortBy.matches("^(date|title|mediaType)$")) {
            throw new BadRequestException("Invalid sort field. Allowed values are: date, title, mediaType");
        }
    }
}
