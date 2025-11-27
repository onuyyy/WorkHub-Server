package com.workhub.cs.entity;

import com.workhub.global.entity.BaseHistoryEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@Table(name = "cs_post_history")
@Entity
public class CsPostHistory extends BaseHistoryEntity {
}
