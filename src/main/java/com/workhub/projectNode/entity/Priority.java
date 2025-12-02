package com.workhub.projectNode.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Priority {
    LOW, MEDIUM, HIGH, CRITICAL;

    @JsonCreator
    public static Priority from(String value) {
        if (value == null) return null;
        return Priority.valueOf(value.toUpperCase());
    }

}
