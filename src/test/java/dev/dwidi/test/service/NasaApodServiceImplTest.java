package dev.dwidi.test.service;

import dev.dwidi.dto.AstronomyPictureResponseDTO;
import dev.dwidi.dto.BaseResponseDTO;
import dev.dwidi.dto.PageDTO;
import dev.dwidi.dto.Pagination;
import dev.dwidi.entity.AstronomyPicture;
import dev.dwidi.proxy.NasaApodClient;
import dev.dwidi.proxy.NasaApodResponse;
import dev.dwidi.repository.AstronomyPictureRepository;
import dev.dwidi.service.NasaApodServiceImpl;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NasaApodServiceImplTest {

    @Mock
    AstronomyPictureRepository repository;

    @Mock
    @RestClient
    NasaApodClient nasaApodClient;

    @InjectMocks
    NasaApodServiceImpl nasaApodService;

    private NasaApodResponse mockResponse;
    private AstronomyPicture mockEntity;
    private AstronomyPictureResponseDTO mockDto;
    private PageDTO<AstronomyPicture> mockPageData;
    private final String API_KEY = "test-api-key";

    @BeforeEach
    void setUp() {
        // Set API key field using reflection
        try {
            var field = NasaApodServiceImpl.class.getDeclaredField("apiKey");
            field.setAccessible(true);
            field.set(nasaApodService, API_KEY);
        } catch (Exception e) {
            fail("Failed to set API key field");
        }

        // Initialize mock response
        mockResponse = new NasaApodResponse();
        mockResponse.setDate(LocalDate.now());
        mockResponse.setTitle("Test APOD");
        mockResponse.setExplanation("Test explanation");
        mockResponse.setUrl("https://example.com/image.jpg");
        mockResponse.setHdurl("https://example.com/image-hd.jpg");
        mockResponse.setMediaType("image");
        mockResponse.setCopyright("Test Copyright");
        mockResponse.setServiceVersion("v1");

        // Initialize mock entity
        mockEntity = new AstronomyPicture();
        mockEntity.setDate(LocalDate.now());
        mockEntity.setTitle("Test APOD");
        mockEntity.setExplanation("Test explanation");
        mockEntity.setUrl("https://example.com/image.jpg");
        mockEntity.setHdurl("https://example.com/image-hd.jpg");
        mockEntity.setMediaType("image");
        mockEntity.setCopyright("Test Copyright");
        mockEntity.setServiceVersion("v1");

        // Initialize mock DTO
        mockDto = new AstronomyPictureResponseDTO(
                "Test Copyright",
                LocalDate.now(),
                "Test explanation",
                "https://example.com/image-hd.jpg",
                "image",
                "v1",
                "Test APOD",
                "https://example.com/image.jpg",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // Initialize pagination
        Pagination pagination = new Pagination(
                0, 10, 100, 10,
                true, false, true, false
        );

        // Initialize page data
        mockPageData = new PageDTO<>(Arrays.asList(mockEntity), pagination);
    }

    @Test
    void testFetchAndSaveApodData_Success() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 2);
        List<NasaApodResponse> mockResponses = Arrays.asList(mockResponse);
        List<AstronomyPicture> mockEntities = Arrays.asList(mockEntity);

        when(nasaApodClient.getApodData(any(), any(), eq(API_KEY)))
                .thenReturn(Uni.createFrom().item(mockResponses));
        when(repository.persistBatch(any()))
                .thenReturn(Uni.createFrom().item(mockEntities));

        // Act
        BaseResponseDTO<List<AstronomyPictureResponseDTO>> result = nasaApodService
                .fetchAndSaveApodData(startDate, endDate)
                .await().indefinitely();

        // Assert
        assertNotNull(result);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatusCode());
        assertEquals("Successfully fetched and saved APOD data", result.getMessage());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals(mockDto.getTitle(), result.getData().get(0).getTitle());
    }

    @Test
    void testFetchAndSaveApodData_EmptyResponse() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 2);

        when(nasaApodClient.getApodData(any(), any(), eq(API_KEY)))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));
        when(repository.persistBatch(any()))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        // Act
        BaseResponseDTO<List<AstronomyPictureResponseDTO>> result = nasaApodService
                .fetchAndSaveApodData(startDate, endDate)
                .await().indefinitely();

        // Assert
        assertNotNull(result);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatusCode());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void testFetchAndSaveApodData_InvalidDateRange() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 1, 2);
        LocalDate endDate = LocalDate.of(2024, 1, 1);

        // Act
        BaseResponseDTO<List<AstronomyPictureResponseDTO>> result = nasaApodService
                .fetchAndSaveApodData(startDate, endDate)
                .await().indefinitely();

        // Assert
        assertNotNull(result);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), result.getStatusCode());
        assertEquals("End date must be after or equal to start date", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testGetAllApodData_Success() {
        // Arrange
        when(repository.findAllPaginated(eq(0), eq(10), eq("date"), eq("DESC")))
                .thenReturn(Uni.createFrom().item(mockPageData));

        // Act
        BaseResponseDTO<PageDTO<AstronomyPictureResponseDTO>> result = nasaApodService
                .getAllApodData(0, 10, "date", "DESC")
                .await().indefinitely();

        // Assert
        assertNotNull(result);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatusCode());
        assertEquals("Successfully retrieved APOD data", result.getMessage());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getContent().size());
        assertTrue(result.getData().getPagination().isFirst());
        assertFalse(result.getData().getPagination().isLast());
        assertTrue(result.getData().getPagination().isHasNext());
        assertFalse(result.getData().getPagination().isHasPrevious());
    }

    @Test
    void testGetAllApodData_EmptyPage() {
        // Arrange
        PageDTO<AstronomyPicture> emptyPage = new PageDTO<>(
                Collections.emptyList(),
                new Pagination(0, 10, 0, 0, true, true, false, false)
        );

        when(repository.findAllPaginated(eq(0), eq(10), eq("date"), eq("DESC")))
                .thenReturn(Uni.createFrom().item(emptyPage));

        // Act
        BaseResponseDTO<PageDTO<AstronomyPictureResponseDTO>> result = nasaApodService
                .getAllApodData(0, 10, "date", "DESC")
                .await().indefinitely();

        // Assert
        assertNotNull(result);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatusCode());
        assertTrue(result.getData().getContent().isEmpty());
        assertTrue(result.getData().getPagination().isFirst());
        assertTrue(result.getData().getPagination().isLast());
    }

    @Test
    void testGetAllApodData_InvalidPageNumber() {
        // Act
        BaseResponseDTO<PageDTO<AstronomyPictureResponseDTO>> result = nasaApodService
                .getAllApodData(-1, 10, "date", "DESC")
                .await().indefinitely();

        // Assert
        assertNotNull(result);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), result.getStatusCode());
        assertNull(result.getData());
    }

    @Test
    void testGetAllApodData_InvalidPageSize() {
        // Act
        BaseResponseDTO<PageDTO<AstronomyPictureResponseDTO>> result = nasaApodService
                .getAllApodData(0, 0, "date", "DESC")
                .await().indefinitely();

        // Assert
        assertNotNull(result);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), result.getStatusCode());
        assertNull(result.getData());
    }

    @Test
    void testGetAllApodData_InvalidSortDirection() {
        // Act
        BaseResponseDTO<PageDTO<AstronomyPictureResponseDTO>> result = nasaApodService
                .getAllApodData(0, 10, "date", "INVALID")
                .await().indefinitely();

        // Assert
        assertNotNull(result);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), result.getStatusCode());
        assertNull(result.getData());
    }

    @Test
    void testGetAllApodData_RepositoryError() {
        // Arrange
        RuntimeException mockException = new RuntimeException("Database error");
        when(repository.findAllPaginated(eq(0), eq(10), eq("date"), eq("DESC")))
                .thenReturn(Uni.createFrom().failure(mockException));

        // Act & Assert
        try {
            BaseResponseDTO<PageDTO<AstronomyPictureResponseDTO>> result = nasaApodService
                    .getAllApodData(0, 10, "date", "DESC")
                    .await().indefinitely();

            assertNotNull(result);
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), result.getStatusCode());
            assertEquals("Error retrieving APOD data: Database error", result.getMessage());
            assertNull(result.getData());
        } catch (Exception e) {
            fail("Should handle repository error gracefully: " + e.getMessage());
        }
    }
}