package com.workhub.project.dto.response;

import com.workhub.userTable.entity.UserTable;
import lombok.Builder;

@Builder
public record DevMembers (
        Long devMemberId,
        String devMemberLoginId,
        String devMemberName,
        String profileImg
){
    public static DevMembers from(UserTable user) {
        return DevMembers.builder()
                .devMemberId(user.getUserId())
                .devMemberLoginId(user.getLoginId())
                .devMemberName(user.getUserName())
                .profileImg(user.getProfileImg())
                .build();
    }
}
