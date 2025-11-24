package com.workhub.userTable.entity;

public enum Status {
    ACTIVE, INACTIVE, SUSPENDED;

    public static Status fromValue(String value){return Enum.valueOf(Status.class, value);}
}
