package dev.dwidi.service;

import dev.dwidi.dto.AstronomyPictureResponseDTO;
import dev.dwidi.dto.BaseResponseDTO;
import dev.dwidi.dto.PageDTO;
import dev.dwidi.entity.AstronomyPicture;
import dev.dwidi.proxy.NasaApodClient;
import dev.dwidi.proxy.NasaApodResponse;
import dev.dwidi.repository.AstronomyPictureRepository;
import dev.dwidi.utils.DateValidator;
import dev.dwidi.utils.PaginationValidator;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ApplicationScoped
public class NasaApodServiceImpl implements NasaApodService {

    private static final Logger LOGGER = Logger.getLogger(NasaApodServiceImpl.class);

    @Inject
    AstronomyPictureRepository repository;

    @Inject
    @RestClient
    NasaApodClient nasaApodClient;

    @ConfigProperty(name = "nasa.api.key")
    String apiKey;

    @Override
    @WithTransaction
    public Uni<BaseResponseDTO<List<AstronomyPictureResponseDTO>>> fetchAndSaveApodData(
            LocalDate startDate, LocalDate endDate) {
        LOGGER.infof("Fetching and saving APOD data for date range: %s to %s", startDate, endDate);

        return DateValidator.<List<AstronomyPictureResponseDTO>>validateDateRange(startDate, endDate)
                .onItem().transformToUni(validationResult -> {
                    if (validationResult != null) {
                        return Uni.createFrom().item(validationResult);
                    }

                    return nasaApodClient.getApodData(
                                    startDate.format(DateTimeFormatter.ISO_DATE),
                                    endDate.format(DateTimeFormatter.ISO_DATE),
                                    apiKey
                            )
                            .ifNoItem().after(Duration.ofMillis(5000)).fail()
                            .onItem().transform(responses -> {
                                LOGGER.infof("Received %d records from NASA API", responses.size());
                                return responses.stream()
                                        .map(this::mapToEntity)
                                        .toList();
                            })
                            .onItem().transformToUni(entities ->
                                    repository.persistBatch(entities)
                            )
                            .onItem().transform(savedEntities ->
                                    savedEntities.stream()
                                            .map(this::mapToDTO)
                                            .toList()
                            )
                            .map(dtos -> new BaseResponseDTO<>(
                                    Response.Status.OK.getStatusCode(),
                                    "Successfully fetched and saved APOD data",
                                    dtos
                            ))
                            .onFailure(TimeoutException.class).recoverWithItem(error ->
                                    new BaseResponseDTO<>(
                                            Response.Status.GATEWAY_TIMEOUT.getStatusCode(),
                                            "Request to NASA API timed out. Please try again later.",
                                            null
                                    )
                            )
                            .onFailure().recoverWithItem(throwable ->
                                    new BaseResponseDTO<>(
                                            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                                            "Error processing request: " + throwable.getMessage(),
                                            null
                                    )
                            );
                });
    }

    @Override
    @WithTransaction
    public Uni<BaseResponseDTO<PageDTO<AstronomyPictureResponseDTO>>> getAllApodData(
            int page, int size, String sortBy, String sortDirection
    ) {
        LOGGER.infof("Retrieving paginated APOD data: page=%d, size=%d, sortBy=%s, sortDirection=%s",
                page, size, sortBy, sortDirection);

        try {
            PaginationValidator.validatePaginationParams(page, size, sortBy, sortDirection);

            return repository.findAllPaginated(page, size, sortBy, sortDirection)
                    .map(pageDTO -> {
                        var dtos = pageDTO.getContent().stream()
                                .map(this::mapToDTO)
                                .toList();

                        LOGGER.infof("Retrieved %d APOD records for page %d", dtos.size(), page);

                        return new BaseResponseDTO<>(
                                Response.Status.OK.getStatusCode(),
                                "Successfully retrieved APOD data",
                                new PageDTO<>(dtos, pageDTO.getPagination())
                        );
                    })
                    .onFailure().invoke(throwable ->
                            LOGGER.error("Failed to retrieve paginated APOD data", throwable)
                    )
                    .onFailure().recoverWithItem(throwable ->
                            new BaseResponseDTO<>(
                                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                                    "Error retrieving APOD data: " + throwable.getMessage(),
                                    null
                            )
                    );
        } catch (BadRequestException e) {
            LOGGER.error("Validation error in pagination parameters", e);
            return Uni.createFrom().item(new BaseResponseDTO<>(
                    Response.Status.BAD_REQUEST.getStatusCode(),
                    e.getMessage(),
                    null
            ));
        }
    }

    private AstronomyPicture mapToEntity(NasaApodResponse response) {
        AstronomyPicture entity = new AstronomyPicture();
        entity.setCopyright(response.getCopyright());
        entity.setDate(response.getDate());
        entity.setExplanation(response.getExplanation());
        entity.setHdurl(response.getHdurl());
        entity.setMediaType(response.getMediaType());
        entity.setServiceVersion(response.getServiceVersion());
        entity.setTitle(response.getTitle());
        entity.setUrl(response.getUrl());
        return entity;
    }

    private AstronomyPictureResponseDTO mapToDTO(AstronomyPicture entity) {
        return new AstronomyPictureResponseDTO(
                entity.getCopyright(),
                entity.getDate(),
                entity.getExplanation(),
                entity.getHdurl(),
                entity.getMediaType(),
                entity.getServiceVersion(),
                entity.getTitle(),
                entity.getUrl(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
