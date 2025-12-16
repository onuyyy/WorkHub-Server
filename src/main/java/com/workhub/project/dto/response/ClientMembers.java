package com.workhub.project.dto.response;

import com.workhub.userTable.entity.UserTable;
import lombok.Builder;

@Builder
public record ClientMembers (
        Long clientMemberId,
        String clientMemberLoginId,
        String clientMemberName,
        String profileImg
){
    public static ClientMembers from(UserTable user) {
        return ClientMembers.builder()
                .clientMemberId(user.getUserId())
                .clientMemberLoginId(user.getLoginId())
                .clientMemberName(user.getUserName())
                .profileImg(user.getProfileImg())
                .build();
    }
}
