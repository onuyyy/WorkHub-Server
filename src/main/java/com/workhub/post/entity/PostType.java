package com.workhub.post.entity;

public enum PostType {

    NOTICE, QUESTION, GENERAL;

    public static PostType of(String value){return Enum.valueOf(PostType.class, value);}
}
