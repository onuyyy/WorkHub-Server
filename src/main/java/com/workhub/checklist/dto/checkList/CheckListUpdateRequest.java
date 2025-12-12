package com.workhub.checklist.dto.checkList;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 체크리스트 업데이트 요청 DTO
 * - 부분 업데이트 지원
 *   변경된 항목만 items 배열에 포함하여 전송
 *   변경되지 않은 항목은 전송하지 않음 (기존 값 유지)
 * - changeType 별 동작
 *   CREATE: 새 항목 추가 (ID 없이 전송, title/content/url 필수)
 *   UPDATE: 기존 항목 수정 (ID 필수, 변경할 필드만 포함, null인 필드는 기존 값 유지)
 *   DELETE: 기존 항목 삭제 (ID만 필요)
 *
 * - 요청 예시
 *   "description": "수정된 설명",
 *   "items": [
 *     {
 *       "changeType": "UPDATE",
 *       "checkListItemId": 1,
 *       "itemTitle": "제목만 수정"
 *       // itemOrder는 null → 기존 값 유지
 *     },
 *     {
 *       "changeType": "DELETE",
 *       "checkListItemId": 3
 *     },
 *     {
 *       "changeType": "CREATE",
 *       "itemTitle": "새 항목",
 *       "itemOrder": 3
 *     }
 *   ]
 * }
 *
 * @param description 체크리스트 설명 (선택, 최대 500자)
 * @param items 변경할 항목 리스트 (변경된 항목만 포함)
 */
public record CheckListUpdateRequest(
        @Size(max = 500, message = "전달사항은 500자 이하여야 합니다")
        String description,

        @Valid
        List<CheckListItemUpdateRequest> items
) {
}
