package com.workhub.checklist.entity.checkList;

import com.workhub.checklist.dto.checkList.CheckListOptionRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "check_list_option")
@Entity
public class CheckListOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_list_option_id")
    private Long checkListOptionId;

    @Column(name = "option_content", length = 300, nullable = false)
    private String optionContent;

    @Column(name = "option_order", nullable = false)
    private Integer optionOrder;

    @Column(name = "check_list_item_id", nullable = false)
    private Long checkListItemId;

    @Column(name = "is_selected", nullable = false)
    private Boolean isSelected;

    public static CheckListOption of(Long checkListItemId, CheckListOptionRequest request) {
        return CheckListOption.builder()
                .optionContent(request.optionContent())
                .optionOrder(request.optionOrder())
                .checkListItemId(checkListItemId)
                .isSelected(false)
                .build();
    }

    /**
     * 체크리스트 선택지 생성 (업데이트용)
     *
     * @param checkListItemId 체크리스트 항목 ID
     * @param optionContent 선택지 내용
     * @param optionOrder 선택지 순서
     * @return 생성된 체크리스트 선택지
     */
    public static CheckListOption of(Long checkListItemId, String optionContent, Integer optionOrder) {
        return CheckListOption.builder()
                .optionContent(optionContent)
                .optionOrder(optionOrder)
                .checkListItemId(checkListItemId)
                .isSelected(false)
                .build();
    }

    public void updateOption(String optionContent, Integer optionOrder) {
        if (optionContent != null) {
            this.optionContent = optionContent;
        }
        if (optionOrder != null) {
            this.optionOrder = optionOrder;
        }
    }

    /**
     * 옵션 선택 상태를 토글한다.
     */
    public void toggleSelection() {
        this.isSelected = !this.isSelected;
    }
}
