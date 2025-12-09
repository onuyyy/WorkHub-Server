package com.workhub.project.repository;

import com.workhub.project.dto.response.ProjectListRequest;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.Status;

import java.time.LocalDate;
import java.util.List;

public interface ProjectRepositoryCustom {

    /**
     * 권한 기반으로 필터링된 프로젝트 목록을 페이징 조회
     *
     * @param projectIds 조회할 프로젝트 ID 리스트 (권한에 따라 필터링됨)
     * @param startDate 계약 시작일 검색 범위 시작
     * @param endDate 계약 시작일 검색 범위 종료
     * @param status 프로젝트 상태 (null이면 전체)
     * @param sortOrder 정렬 조건
     * @param cursor 커서 (마지막 조회한 projectId)
     * @param size 페이지 크기 (실제로는 size + 1 조회)
     * @return 페이징된 프로젝트 목록
     */
    List<Project> findProjectsWithPaging(List<Long> projectIds, LocalDate startDate, LocalDate endDate,
            Status status, ProjectListRequest.SortOrder sortOrder, Long cursor, int size);
}
