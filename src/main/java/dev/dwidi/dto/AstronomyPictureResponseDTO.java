package dev.dwidi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AstronomyPictureResponseDTO {
    private String copyright;
    private LocalDate date;
    private String explanation;
    private String hdurl;
    private String mediaType;
    private String serviceVersion;
    private String title;
    private String url;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
