package com.workhub.project.dto.request;

import com.workhub.project.entity.Status;
import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Builder
public class ProjectListRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    private Status status;

    @Builder.Default
    private SortOrder sortOrder = SortOrder.LATEST;

    private Long cursor;

    @Builder.Default
    private Integer size = 9;

    public enum SortOrder {
        LATEST,     // 최신순 (현재 날짜에 가까운 순)
        OLDEST      // 오래된 순 (현재 날짜에 먼 순)
    }

    /**
     * 날짜 범위가 지정되지 않은 경우 기본값 설정
     * startDate가 null이면 현재 날짜 기준 1년 전으로 설정
     * endDate가 null이면 현재 날짜로 설정
     */
    public void applyDefaultDateRange() {
        LocalDate now = LocalDate.now();
        if (this.startDate == null) {
            this.startDate = now.minusYears(1);
        }
        if (this.endDate == null) {
            this.endDate = now;
        }
    }

    /**
     * 페이지 크기 제한 (최대 100개)
     */
    public void validateAndAdjustSize() {
        if (this.size == null || this.size <= 0) {
            this.size = 9;
        }
        if (this.size > 100) {
            this.size = 100;
        }
    }

    public static ProjectListRequest from(LocalDate startDate, LocalDate endDate, Status status,
                                          ProjectListRequest.SortOrder sortOrder, Long cursor, Integer size) {

        return ProjectListRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .status(status)
                .sortOrder(sortOrder)
                .cursor(cursor)
                .size(size)
                .build();
    }
}
