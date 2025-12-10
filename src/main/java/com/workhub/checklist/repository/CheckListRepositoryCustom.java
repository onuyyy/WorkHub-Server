package com.workhub.checklist.repository;

import com.workhub.checklist.dto.CheckListDetails;

public interface CheckListRepositoryCustom {

    /**
     * CheckList의 전체 계층 구조를 효율적으로 조회한다.
     * Item, Option, File을 모두 포함
     *
     * @param checkListId 체크리스트 ID
     * @return 체크리스트 계층 구조
     */
    CheckListDetails findCheckListDetailsById(Long checkListId);
}