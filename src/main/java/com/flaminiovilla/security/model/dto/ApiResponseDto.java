package com.flaminiovilla.security.model.dto;

/**
 * The type API response DTO to store the response of the API.
 */
public class ApiResponseDto {
    // Whether the request was successful or not
    private boolean success;

    // The message to be returned
    private String message;

    /**
     * Instantiates a new Api response dto.
     * @param success the success to be set
     * @param message the message to be set
     */
    public ApiResponseDto(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Gets success or not
     * @return true or false
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets success
     * @param success true or false to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Message getter
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Message setter
     * @param message the message to be set
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
