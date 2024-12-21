package dev.dwidi.proxy;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/planetary")
@RegisterRestClient(configKey = "nasa-api")
@ApplicationScoped
public interface NasaApodClient {
    @GET
    @Path("/apod")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<NasaApodResponse>> getApodData(
            @QueryParam("start_date") String startDate,
            @QueryParam("end_date") String endDate,
            @QueryParam("api_key") String apiKey
    );
}
