package com.cassiomolin.example.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * API model that represents an API error.
 *
 * @author cassiomolin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private Long timestamp;

    private Integer status;

    private String error;

    private String message;

    private String path;

    public ApiError() {

    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}