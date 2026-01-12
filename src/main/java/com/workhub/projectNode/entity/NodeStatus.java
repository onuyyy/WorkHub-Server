package com.workhub.projectNode.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum NodeStatus {
    NOT_STARTED, IN_PROGRESS, PENDING_REVIEW, ON_HOLD, DELETED, DONE;

    @JsonCreator
    public static NodeStatus from(String value) {
        if (value == null) return null;
        return NodeStatus.valueOf(value.toUpperCase().replace("-", "_"));
    }
}
