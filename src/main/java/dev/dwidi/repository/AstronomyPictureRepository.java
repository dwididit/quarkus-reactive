package dev.dwidi.repository;

import dev.dwidi.dto.PageDTO;
import dev.dwidi.dto.Pagination;
import dev.dwidi.entity.AstronomyPicture;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;

import static io.quarkus.hibernate.reactive.panache.Panache.withTransaction;

@ApplicationScoped
public class AstronomyPictureRepository implements PanacheRepository<AstronomyPicture> {

    private static final Logger LOGGER = Logger.getLogger(AstronomyPictureRepository.class);

    public Uni<List<AstronomyPicture>> persistBatch(List<AstronomyPicture> entities) {
        return withTransaction(() ->
                Uni.join().all(
                                entities.stream()
                                        .map(this::persist)
                                        .toList()
                        ).andCollectFailures()
                        .replaceWith(entities)
        );
    }

    public Uni<PageDTO<AstronomyPicture>> findAllPaginated(int page, int size, String sortBy, String sortDirection) {
        LOGGER.infof("Fetching paginated data: page=%d, size=%d, sortBy=%s, sortDirection=%s", page, size, sortBy, sortDirection);

        String validSortField = validateSortField(sortBy);
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.Ascending : Sort.Direction.Descending;
        Sort sort = Sort.by(validSortField).direction(direction);

        return Uni.combine().all().unis(
                        count(),
                        findAll(sort)
                                .page(Page.of(page, size))
                                .list()
                ).asTuple()
                .map(tuple -> {
                    Long total = tuple.getItem1();
                    var content = tuple.getItem2();
                    int totalPages = (int) Math.ceil((double) total / size);

                    LOGGER.infof("Retrieved %d records, total elements=%d, total pages=%d",
                            content.size(), total, totalPages);

                    var pagination = new Pagination(
                            page,
                            size,
                            total,
                            totalPages,
                            page == 0,
                            page >= totalPages - 1,
                            page < totalPages - 1,
                            page > 0
                    );

                    return new PageDTO<>(content, pagination);
                });
    }

    public String validateSortField(String sortBy) {
        LOGGER.debugf("Validating sort field: %s", sortBy);
        return switch (sortBy) {
            case "date", "title", "mediaType" -> sortBy;
            default -> "date";
        };
    }
}