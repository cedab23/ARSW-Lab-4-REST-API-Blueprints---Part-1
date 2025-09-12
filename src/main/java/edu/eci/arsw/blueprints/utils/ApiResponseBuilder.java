package edu.eci.arsw.blueprints.utils;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import edu.eci.arsw.blueprints.model.dto.BaseApiResponse;

@Component
public class ApiResponseBuilder {
    public <T> BaseApiResponse<T> success(T data, String message) {
        return new BaseApiResponse<T>(HttpStatus.OK.value(), message, data);
    }

    public <T> BaseApiResponse<T> created(T data, String message) {
        return new BaseApiResponse<T>(HttpStatus.CREATED.value(), message, data);
    }

    public <T> BaseApiResponse<T> accepted(T data, String message) {
        return new BaseApiResponse<T>(HttpStatus.ACCEPTED.value(), message, data);
    }

    public <T> BaseApiResponse<T> badRequest(T data, String message) {
        return new BaseApiResponse<T>(HttpStatus.BAD_REQUEST.value(), message, data);
    }

    public <T> BaseApiResponse<T> notFound(T data, String message) {
        return new BaseApiResponse<T>(HttpStatus.NOT_FOUND.value(), message, data);
    }

    public <T> BaseApiResponse<T> internalServerError(T data, String message) {
        return new BaseApiResponse<T>(HttpStatus.INTERNAL_SERVER_ERROR.value(), message, data);
    }

    public <T> BaseApiResponse<T> forbidden(T data, String message) {
        return new BaseApiResponse<T>(HttpStatus.FORBIDDEN.value(), message, data);
    }
}