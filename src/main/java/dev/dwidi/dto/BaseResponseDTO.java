package dev.dwidi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponseDTO<T> {
    private Integer statusCode;
    private String message;
    private T data;
}
