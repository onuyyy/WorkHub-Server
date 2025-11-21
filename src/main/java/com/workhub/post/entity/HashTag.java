package com.workhub.post.entity;

public enum HashTag {

   REQ_DEF, WIREFRAME, DESIGN, PUBLISHING, DEVELOPMENT, QA;

   public static HashTag of(String value){return Enum.valueOf(HashTag.class, value); }
}
