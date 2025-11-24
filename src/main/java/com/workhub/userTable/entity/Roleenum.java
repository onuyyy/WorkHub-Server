package com.workhub.userTable.entity;

public enum Roleenum {
    CLIENT,  DEVELOPER, ADMIN;

    public static Roleenum fromValue(String value){return Enum.valueOf(Roleenum.class, value);}
    }

