package com.workhub.checklist.entity;

import com.workhub.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "check_list_item_file")
@Entity
public class CheckListItemFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_list_item_file_id")
    private Long checkListItemFileId;

    @Column(name = "file_url", length = 255, nullable = false)
    private String fileUrl;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "file_order")
    private Integer fileOrder;

    @Column(name = "check_list_item_id", nullable = false)
    private Long checkListItemId;
}
