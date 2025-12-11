package com.workhub.checklist.entity;

import com.workhub.checklist.dto.CheckListItemRequest;
import com.workhub.checklist.dto.CheckListItemStatus;
import com.workhub.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "check_list_item")
@Entity
public class CheckListItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_list_item_id")
    private Long checkListItemId;

    @Column(name = "item_title", length = 50, nullable = false)
    private String itemTitle;

    @Column(name = "item_order", nullable = false)
    private Integer itemOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CheckListItemStatus status;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "check_list_id")
    private Long checkListId;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "user_id")
    private Long userId;

    public static CheckListItem of(Long checkListId, CheckListItemRequest request, Long userId) {
        return CheckListItem.builder()
                .itemTitle(request.itemTitle())
                .itemOrder(request.itemOrder())
                .checkListId(checkListId)
                .templateId(request.templateId())
                .userId(userId)
                .status(CheckListItemStatus.PENDING)
                .build();
    }

    /**
     * 체크리스트 항목 생성 (업데이트용)
     *
     * @param checkListId 체크리스트 ID
     * @param itemTitle 항목 제목
     * @param itemOrder 항목 순서
     * @param templateId 템플릿 ID (선택)
     * @param userId 생성자 ID
     * @return 생성된 체크리스트 항목
     */
    public static CheckListItem of(Long checkListId, String itemTitle, Integer itemOrder, Long templateId, Long userId) {
        return CheckListItem.builder()
                .itemTitle(itemTitle)
                .itemOrder(itemOrder)
                .checkListId(checkListId)
                .templateId(templateId)
                .userId(userId)
                .build();
    }

    public void updateItem(String itemTitle, Integer itemOrder, Long templateId) {
        if (itemTitle != null) {
            this.itemTitle = itemTitle;
        }
        if (itemOrder != null) {
            this.itemOrder = itemOrder;
        }
        if (templateId != null) {
            this.templateId = templateId;
        }
    }

    public void updateStatus(CheckListItemStatus status) {
        if (status != null) {
            this.status = status;
        }
    }
}
