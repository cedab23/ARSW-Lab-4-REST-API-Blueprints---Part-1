package edu.eci.arsw.blueprints.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BaseApiResponse<T>(int code, String message, T data) {

}
