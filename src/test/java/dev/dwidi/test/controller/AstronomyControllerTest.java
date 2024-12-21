package dev.dwidi.test.controller;

import dev.dwidi.controller.AstronomyController;
import dev.dwidi.dto.*;
import dev.dwidi.service.NasaApodService;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AstronomyControllerTest {

    @Mock
    private NasaApodService nasaApodService;

    @InjectMocks
    private AstronomyController astronomyController;

    private AstronomyPictureResponseDTO mockApodData;
    private PageDTO<AstronomyPictureResponseDTO> mockPageData;

    @BeforeEach
    void setUp() {
        // Create mock APOD data
        mockApodData = new AstronomyPictureResponseDTO();
        mockApodData.setTitle("Test APOD");
        mockApodData.setExplanation("Test explanation");
        mockApodData.setDate(LocalDate.now());

        // Create pagination data
        Pagination pagination = new Pagination();
        pagination.setPageNumber(0);
        pagination.setPageSize(10);
        pagination.setTotalElements(100);
        pagination.setTotalPages(10);
        pagination.setFirst(true);
        pagination.setLast(false);
        pagination.setHasNext(true);
        pagination.setHasPrevious(false);

        // Create page data
        List<AstronomyPictureResponseDTO> content = Arrays.asList(mockApodData);
        mockPageData = new PageDTO<>(content, pagination);
    }

    @Test
    void testFetchAndSaveApodData_Success() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 2);
        List<AstronomyPictureResponseDTO> mockResponse = Arrays.asList(mockApodData);
        BaseResponseDTO<List<AstronomyPictureResponseDTO>> expectedResponse =
                new BaseResponseDTO<>(200, "Success", mockResponse);

        when(nasaApodService.fetchAndSaveApodData(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Uni.createFrom().item(expectedResponse));

        // Act
        Uni<BaseResponseDTO<List<AstronomyPictureResponseDTO>>> result =
                astronomyController.fetchAndSaveApodData(
                        startDate.toString(),
                        endDate.toString()
                );

        // Assert
        result.subscribe().with(response -> {
            assertEquals(200, response.getStatusCode());
            assertNotNull(response.getData());
            assertEquals(1, response.getData().size());
            assertEquals("Test APOD", response.getData().get(0).getTitle());
        });
    }

    @Test
    void testFetchAndSaveApodData_InvalidDateFormat() {
        // Arrange
        String invalidStartDate = "2024-13-45";
        String validEndDate = "2024-01-02";

        // Act
        Uni<BaseResponseDTO<List<AstronomyPictureResponseDTO>>> result =
                astronomyController.fetchAndSaveApodData(invalidStartDate, validEndDate);

        // Assert
        result.subscribe().with(response -> {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode());
            assertEquals("Invalid date format. Please use YYYY-MM-DD format", response.getMessage());
            assertNull(response.getData());
        });
    }

    @Test
    void testFetchAndSaveApodData_StartDateAfterEndDate() {
        // Arrange
        String startDate = "2024-12-31";
        String endDate = "2024-01-01";

        // Set up mock service to return error response
        BaseResponseDTO<List<AstronomyPictureResponseDTO>> errorResponse =
                new BaseResponseDTO<>(400, "Start date must be before end date", null);
        when(nasaApodService.fetchAndSaveApodData(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Uni.createFrom().item(errorResponse));

        // Act
        Uni<BaseResponseDTO<List<AstronomyPictureResponseDTO>>> result =
                astronomyController.fetchAndSaveApodData(startDate, endDate);

        // Assert
        result.subscribe().with(response -> {
            assertEquals(400, response.getStatusCode());
            assertEquals("Start date must be before end date", response.getMessage());
            assertNull(response.getData());
        });
    }

    @Test
    void testFetchAndSaveApodData_EmptyResponse() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 2);
        BaseResponseDTO<List<AstronomyPictureResponseDTO>> emptyResponse =
                new BaseResponseDTO<>(200, "Success", Collections.emptyList());

        when(nasaApodService.fetchAndSaveApodData(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Uni.createFrom().item(emptyResponse));

        // Act
        Uni<BaseResponseDTO<List<AstronomyPictureResponseDTO>>> result =
                astronomyController.fetchAndSaveApodData(
                        startDate.toString(),
                        endDate.toString()
                );

        // Assert
        result.subscribe().with(response -> {
            assertEquals(200, response.getStatusCode());
            assertNotNull(response.getData());
            assertTrue(response.getData().isEmpty());
        });
    }

    @Test
    void testGetAllApodData_Success() {
        // Arrange
        BaseResponseDTO<PageDTO<AstronomyPictureResponseDTO>> expectedResponse =
                new BaseResponseDTO<>(200, "Success", mockPageData);

        when(nasaApodService.getAllApodData(eq(0), eq(10), eq("date"), eq("asc")))
                .thenReturn(Uni.createFrom().item(expectedResponse));

        // Act
        Uni<BaseResponseDTO<PageDTO<AstronomyPictureResponseDTO>>> result =
                astronomyController.getAllApodData(0, 10, "date", "asc");

        // Assert
        result.subscribe().with(response -> {
            assertEquals(200, response.getStatusCode());
            assertNotNull(response.getData());
            assertEquals(1, response.getData().getContent().size());
            assertEquals(0, response.getData().getPagination().getPageNumber());
            assertEquals(10, response.getData().getPagination().getPageSize());
            assertTrue(response.getData().getPagination().isFirst());
            assertFalse(response.getData().getPagination().isLast());
        });
    }

    @Test
    void testGetAllApodData_NegativePage() {
        // Arrange
        BaseResponseDTO<PageDTO<AstronomyPictureResponseDTO>> errorResponse =
                new BaseResponseDTO<>(400, "Page number cannot be negative", null);

        when(nasaApodService.getAllApodData(eq(-1), anyInt(), anyString(), anyString()))
                .thenReturn(Uni.createFrom().item(errorResponse));

        // Act
        Uni<BaseResponseDTO<PageDTO<AstronomyPictureResponseDTO>>> result =
                astronomyController.getAllApodData(-1, 10, "date", "asc");

        // Assert
        result.subscribe().with(response -> {
            assertEquals(400, response.getStatusCode());
            assertEquals("Page number cannot be negative", response.getMessage());
            assertNull(response.getData());
        });
    }

    @Test
    void testGetAllApodData_InvalidPageSize() {
        // Arrange
        BaseResponseDTO<PageDTO<AstronomyPictureResponseDTO>> errorResponse =
                new BaseResponseDTO<>(400, "Page size must be between 1 and 100", null);

        when(nasaApodService.getAllApodData(anyInt(), eq(0), anyString(), anyString()))
                .thenReturn(Uni.createFrom().item(errorResponse));

        // Act
        Uni<BaseResponseDTO<PageDTO<AstronomyPictureResponseDTO>>> result =
                astronomyController.getAllApodData(0, 0, "date", "asc");

        // Assert
        result.subscribe().with(response -> {
            assertEquals(400, response.getStatusCode());
            assertEquals("Page size must be between 1 and 100", response.getMessage());
            assertNull(response.getData());
        });
    }

    @Test
    void testGetAllApodData_InvalidSortDirection() {
        // Arrange
        BaseResponseDTO<PageDTO<AstronomyPictureResponseDTO>> errorResponse =
                new BaseResponseDTO<>(400, "Sort direction must be 'asc' or 'desc'", null);

        when(nasaApodService.getAllApodData(anyInt(), anyInt(), anyString(), eq("invalid")))
                .thenReturn(Uni.createFrom().item(errorResponse));

        // Act
        Uni<BaseResponseDTO<PageDTO<AstronomyPictureResponseDTO>>> result =
                astronomyController.getAllApodData(0, 10, "date", "invalid");

        // Assert
        result.subscribe().with(response -> {
            assertEquals(400, response.getStatusCode());
            assertEquals("Sort direction must be 'asc' or 'desc'", response.getMessage());
            assertNull(response.getData());
        });
    }

    @Test
    void testGetAllApodData_DefaultPagination() {
        // Arrange
        BaseResponseDTO<PageDTO<AstronomyPictureResponseDTO>> expectedResponse =
                new BaseResponseDTO<>(200, "Success", mockPageData);

        when(nasaApodService.getAllApodData(eq(0), eq(10), isNull(), isNull()))
                .thenReturn(Uni.createFrom().item(expectedResponse));

        // Act
        Uni<BaseResponseDTO<PageDTO<AstronomyPictureResponseDTO>>> result =
                astronomyController.getAllApodData(0, 10, null, null);

        // Assert
        result.subscribe().with(response -> {
            assertEquals(200, response.getStatusCode());
            assertNotNull(response.getData());
            assertEquals(1, response.getData().getContent().size());
            assertTrue(response.getData().getPagination().isFirst());
            assertTrue(response.getData().getPagination().isHasNext());
            assertFalse(response.getData().getPagination().isHasPrevious());
        });
    }
}