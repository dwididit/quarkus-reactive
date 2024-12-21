package dev.dwidi.service;

import dev.dwidi.dto.AstronomyPictureResponseDTO;
import dev.dwidi.dto.BaseResponseDTO;
import dev.dwidi.dto.PageDTO;
import io.smallrye.mutiny.Uni;

import java.time.LocalDate;
import java.util.List;

public interface NasaApodService {
    Uni<BaseResponseDTO<List<AstronomyPictureResponseDTO>>> fetchAndSaveApodData(LocalDate startDate, LocalDate endDate);
    Uni<BaseResponseDTO<PageDTO<AstronomyPictureResponseDTO>>> getAllApodData(
            int page,
            int size,
            String sortBy,
            String sortDirection
    );
}