package com.workhub.userTable.entity;

public enum UserRole {
    CLIENT,  DEVELOPER, ADMIN;

    public static UserRole fromValue(String value){return Enum.valueOf(UserRole.class, value);}
    }

