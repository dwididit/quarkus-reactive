package dev.dwidi.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "astronomy_picture")
@Getter
@Setter
public class AstronomyPicture extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "copyright", length = 500)
    private String copyright;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "explanation", length = 5000)
    private String explanation;

    @Column(name = "hdurl")
    private String hdurl;

    @Column(name = "media_type")
    private String mediaType;

    @Column(name = "service_version")
    private String serviceVersion;

    @Column(name = "title")
    private String title;

    @Column(name = "url")
    private String url;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

