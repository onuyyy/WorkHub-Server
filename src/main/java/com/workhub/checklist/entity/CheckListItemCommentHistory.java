package com.workhub.checklist.entity;

import com.workhub.global.entity.BaseHistoryEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@Table(name = "check_list_item_comment_history")
@Entity
public class CheckListItemCommentHistory extends BaseHistoryEntity {
}
