package com.workhub.project.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record PagedProjectListResponse(
        List<ProjectListResponse> projects,
        Long nextCursor,
        Boolean hasNext,
        Integer size
) {
    /**
     * 페이징된 프로젝트 목록 응답 생성
     *
     * @param projects 프로젝트 목록
     * @param requestedSize 요청한 페이지 크기
     * @return 페이징 정보가 포함된 응답 객체
     */
    public static PagedProjectListResponse from(List<ProjectListResponse> projects, int requestedSize) {

        boolean hasNext = projects.size() > requestedSize;

        // size보다 많이 조회한 경우 마지막 항목 제거
        List<ProjectListResponse> resultProjects = hasNext
            ? projects.subList(0, requestedSize)
            : projects;

        // 다음 커서는 마지막 항목의 projectId
        Long nextCursor = hasNext && !resultProjects.isEmpty()
            ? resultProjects.getLast().projectId()
            : null;

        return PagedProjectListResponse.builder()
                .projects(resultProjects)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(resultProjects.size())
                .build();
    }

    public static PagedProjectListResponse emptyResponse() {
        return PagedProjectListResponse.builder()
                .projects(List.of())
                .nextCursor(null)
                .hasNext(false)
                .size(0)
                .build();
    }
}
