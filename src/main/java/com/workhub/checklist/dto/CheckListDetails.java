package com.workhub.checklist.dto;

import com.workhub.checklist.entity.CheckList;
import com.workhub.checklist.entity.CheckListItem;
import com.workhub.checklist.entity.CheckListOption;
import com.workhub.checklist.entity.CheckListOptionFile;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * CheckList의 전체 계층 구조를 담는 DTO
 * Repository 계층에서 한 번에 조회한 데이터를 담아 Service로 전달한다.
 */
@Getter
@AllArgsConstructor
public class CheckListDetails {
    private CheckList checkList;
    private List<CheckListItem> items;
    private List<CheckListOption> options;
    private List<CheckListOptionFile> files;
}