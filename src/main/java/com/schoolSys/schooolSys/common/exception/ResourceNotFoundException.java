package com.schoolSys.schooolSys.common.exception;

import java.util.UUID;

/**
 * Thrown when a requested resource (entity) cannot be found in the database.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * @param resource the type of resource (e.g. "Student", "Course")
     * @param id       the ID that was looked up
     */
    public ResourceNotFoundException(String resource, UUID id) {
        super(resource + " not found with id: " + id);
    }

    /**
     * @param message a descriptive not-found message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
