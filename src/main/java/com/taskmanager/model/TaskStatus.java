package com.taskmanager.model;

/**
 * Valid states for a Task. Using an enum instead of a free-text string
 * means the database and Bean Validation both reject invalid values
 * automatically - "TODOO" or "done" (lowercase) simply won't deserialize.
 */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE
}
