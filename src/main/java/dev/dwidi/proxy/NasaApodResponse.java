package dev.dwidi.proxy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NasaApodResponse {

    @JsonProperty(value = "copyright", required = false)
    private String copyright;

    @JsonProperty(required = true)
    private LocalDate date;

    @JsonProperty(required = false)
    private String explanation;

    @JsonProperty(required = false)
    private String hdurl;

    @JsonProperty(value = "media_type", required = false)
    private String mediaType;

    @JsonProperty(value = "service_version", required = false)
    private String serviceVersion;

    @JsonProperty(required = false)
    private String title;

    @JsonProperty(required = false)
    private String url;
}
