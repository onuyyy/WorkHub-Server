package com.workhub.checklist.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.workhub.checklist.dto.checkList.CheckListDetails;
import com.workhub.checklist.entity.checkList.CheckList;
import com.workhub.checklist.entity.checkList.CheckListItem;
import com.workhub.checklist.entity.checkList.CheckListOption;
import com.workhub.checklist.entity.checkList.CheckListOptionFile;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.workhub.checklist.entity.checkList.QCheckList.checkList;
import static com.workhub.checklist.entity.checkList.QCheckListItem.checkListItem;
import static com.workhub.checklist.entity.checkList.QCheckListOption.checkListOption;
import static com.workhub.checklist.entity.checkList.QCheckListOptionFile.checkListOptionFile;

@Repository
@RequiredArgsConstructor
public class CheckListRepositoryImpl implements CheckListRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public CheckListDetails findCheckListDetailsById(Long checkListId) {
        // 1. CheckList 조회
        CheckList foundCheckList = queryFactory
                .selectFrom(checkList)
                .where(checkList.checkListId.eq(checkListId))
                .fetchOne();

        if (foundCheckList == null) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_CHECK_LIST);
        }

        // 2. CheckListItem 조회 (정렬 포함)
        List<CheckListItem> items = queryFactory
                .selectFrom(checkListItem)
                .where(checkListItem.checkListId.eq(checkListId))
                .orderBy(checkListItem.itemOrder.asc())
                .fetch();

        if (items.isEmpty()) {
            return new CheckListDetails(foundCheckList, items, List.of(), List.of());
        }

        // 3. CheckListOption 조회 - IN 절 사용
        List<Long> itemIds = items.stream()
                .map(CheckListItem::getCheckListItemId)
                .toList();

        List<CheckListOption> options = queryFactory
                .selectFrom(checkListOption)
                .where(checkListOption.checkListItemId.in(itemIds))
                .orderBy(
                        checkListOption.checkListItemId.asc(),
                        checkListOption.optionOrder.asc()
                )
                .fetch();

        if (options.isEmpty()) {
            return new CheckListDetails(foundCheckList, items, options, List.of());
        }

        // 4. CheckListOptionFile 조회 - IN 절 사용
        List<Long> optionIds = options.stream()
                .map(CheckListOption::getCheckListOptionId)
                .toList();

        List<CheckListOptionFile> files = queryFactory
                .selectFrom(checkListOptionFile)
                .where(checkListOptionFile.checkListOptionId.in(optionIds))
                .orderBy(
                        checkListOptionFile.checkListOptionId.asc(),
                        checkListOptionFile.fileOrder.asc()
                )
                .fetch();

        return new CheckListDetails(foundCheckList, items, options, files);
    }
}