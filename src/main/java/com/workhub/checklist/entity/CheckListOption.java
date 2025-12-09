package com.workhub.checklist.entity;

import com.workhub.checklist.dto.CheckListOptionRequest;
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

    public static CheckListOption of(Long checkListItemId, CheckListOptionRequest request) {
        return CheckListOption.builder()
                .optionContent(request.optionContent())
                .optionOrder(request.optionOrder())
                .checkListItemId(checkListItemId)
                .build();
    }
}
