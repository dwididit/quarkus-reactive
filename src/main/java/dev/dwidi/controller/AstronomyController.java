package dev.dwidi.controller;

import dev.dwidi.dto.AstronomyPictureResponseDTO;
import dev.dwidi.dto.BaseResponseDTO;
import dev.dwidi.dto.PageDTO;
import dev.dwidi.service.NasaApodService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Path("/api/v1/astronomy")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Astronomy", description = "NASA Astronomy Picture of the Day operations")
public class AstronomyController {

    @Inject
    NasaApodService nasaApodService;

    @GET
    @Path("/fetch")
    public Uni<BaseResponseDTO<List<AstronomyPictureResponseDTO>>> fetchAndSaveApodData(
            @Parameter(description = "Start date in YYYY-MM-DD format")
            @QueryParam("startDate") String startDate,
            @Parameter(description = "End date in YYYY-MM-DD format")
            @QueryParam("endDate") String endDate
    ) {
        try {
            LocalDate parsedStartDate = LocalDate.parse(startDate);
            LocalDate parsedEndDate = LocalDate.parse(endDate);

            return nasaApodService.fetchAndSaveApodData(parsedStartDate, parsedEndDate);
        } catch (DateTimeParseException e) {
            return Uni.createFrom().item(new BaseResponseDTO<>(
                    Response.Status.BAD_REQUEST.getStatusCode(),
                    "Invalid date format. Please use YYYY-MM-DD format",
                    null
            ));
        }
    }

    @GET
    @Operation(
            summary = "Get paginated APOD data",
            description = "Retrieves a paginated list of astronomy pictures with sorting options"
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Successfully retrieved APOD data",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = BaseResponseDTO.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid pagination parameters"
            )
    })
    public Uni<BaseResponseDTO<PageDTO<AstronomyPictureResponseDTO>>> getAllApodData(
            @Parameter(description = "Page number (0-based)")
            @QueryParam("page") @DefaultValue("0") int page,

            @Parameter(description = "Number of items per page")
            @QueryParam("size") @DefaultValue("10") int size,

            @Parameter(description = "Field to sort by (e.g., date, title)")
            @QueryParam("sortBy") String sortBy,

            @Parameter(description = "Sort direction (asc or desc)")
            @QueryParam("sortDirection") String sortDirection
    ) {
        return nasaApodService.getAllApodData(page, size, sortBy, sortDirection);
    }
}