package com.workhub.checklist.dto.checkList;

import com.workhub.checklist.entity.checkList.CheckList;
import com.workhub.checklist.entity.checkList.CheckListItem;
import com.workhub.checklist.entity.checkList.CheckListOption;
import com.workhub.checklist.entity.checkList.CheckListOptionFile;
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