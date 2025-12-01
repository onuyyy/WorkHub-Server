package com.workhub.project.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum  Status {
    CONTRACT, IN_PROGRESS, DELIVERY, MAINTENANCE, COMPLETED, CANCELLED, DELETED;

    @JsonCreator
    public static Status from(String value) {
        if (value == null) return null;
        return Status.valueOf(value.toUpperCase().replace("-", "_"));
    }

    @JsonCreator
    public String to(String value) {
        return this.name();
    }
}
