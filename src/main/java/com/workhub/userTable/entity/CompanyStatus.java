package com.workhub.userTable.entity;

public enum CompanyStatus {
    ACTIVE, INACTIVE, SUSPENDED;

    public static CompanyStatus fromValue(String value) {
        return CompanyStatus.valueOf(value);
    }
}
