package com.workhub.project.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.project.dto.request.ProjectListRequest;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.ProjectClientMember;
import com.workhub.project.entity.ProjectDevMember;
import com.workhub.project.entity.Status;
import com.workhub.project.repository.ClientMemberRepository;
import com.workhub.project.repository.DevMemberRepository;
import com.workhub.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ClientMemberRepository clientMemberRepository;
    private final DevMemberRepository devMemberRepository;

    public Project saveProject(Project project){
        return projectRepository.save(project);
    }

    public Project findProjectById(Long id){
        return projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    public List<ProjectClientMember> saveProjectClientMember(List<ProjectClientMember> projectClientMembers){
        return clientMemberRepository.saveAll(projectClientMembers);
    }

    public List<ProjectDevMember> saveProjectDevMember(List<ProjectDevMember> projectDevMembers){
        return devMemberRepository.saveAll(projectDevMembers);
    }

    public Project validateCompletedProject(Long projectId) {
        Project project = findProjectById(projectId);
        if (!Status.COMPLETED.equals(project.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PROJECT_STATUS_FOR_CS_POST);
        }
        return project;
    }

    public Project validateProject(Long projectId) {
        Project project = findProjectById(projectId);
        if (!Status.IN_PROGRESS.equals(project.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PROJECT_STATUS_FOR_POST);
        }
        return project;
    }

    public List<ProjectClientMember> getClientMemberByUserId(Long userId) {
        return clientMemberRepository.findByUserId(userId);
    }

    public List<ProjectDevMember> getDevMemberByUserId(Long userId) {
        return devMemberRepository.findByUserId(userId);
    }

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public Long countInProgressOrCompletedProjects() {
        return projectRepository.countByStatusIn(List.of(Status.IN_PROGRESS, Status.COMPLETED));
    }

    public Long countInProgressProjects() {
        return projectRepository.countByStatus(Status.IN_PROGRESS);
    }

    public List<ProjectClientMember> getClientMemberByProjectIdIn(List<Long> projectIds) {
        return clientMemberRepository.findByProjectIdIn(projectIds);
    }

    public List<ProjectDevMember> getDevMemberByProjectIdIn(List<Long> projectIds) {
        return devMemberRepository.findByProjectIdIn(projectIds);
    }

    public List<ProjectClientMember> getClientMemberByProjectId(Long projectId) {
        return clientMemberRepository.findByProjectIdIn(List.of(projectId));
    }

    public List<ProjectDevMember> getDevMemberByProjectId(Long projectId) {
        return devMemberRepository.findByProjectIdIn(List.of(projectId));
    }

    public Long countProjectsOverlapping(LocalDate monthStart, LocalDate monthEnd) {
        return projectRepository.countProjectsOverlapping(monthStart, monthEnd);
    }

    public void validateDevMemberForProject(Long projectId, Long devMemberId) {
        if(!devMemberRepository.existsByProjectIdAndUserId(projectId, devMemberId)) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_DEV_MEMBER);
        }
    }

    public void validateClientMemberForProject(Long projectId, Long clientMemberId) {
        if(!clientMemberRepository.existsByProjectIdAndUserId(projectId, clientMemberId)) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }
    }

    public void validateProjectMember(Long projectId, Long userId) {
        boolean isDevMember = devMemberRepository.existsByProjectIdAndUserId(projectId, userId);
        boolean isClientMember = clientMemberRepository.existsByProjectIdAndUserId(projectId, userId);

        if (!isDevMember && !isClientMember) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }
    }

    /**
     * 클라이언트 멤버를 저장
     * @param userIds 클라이언트 멤버 사용자 ID 리스트
     * @param projectId 프로젝트 ID
     * @return 저장된 클라이언트 멤버 리스트
     */
    public List<ProjectClientMember> saveClientMembers(List<Long> userIds, Long projectId) {
        List<ProjectClientMember> clientMembers = userIds.stream()
                .map(userId -> ProjectClientMember.of(userId, projectId))
                .toList();
        return saveProjectClientMember(clientMembers);
    }

    /**
     * 개발사 멤버를 저장
     * @param userIds 개발사 멤버 사용자 ID 리스트
     * @param projectId 프로젝트 ID
     * @return 저장된 개발사 멤버 리스트
     */
    public List<ProjectDevMember> saveDevMembers(List<Long> userIds, Long projectId) {
        List<ProjectDevMember> devMembers = userIds.stream()
                .map(userId -> ProjectDevMember.of(userId, projectId))
                .toList();
        return saveProjectDevMember(devMembers);
    }

    /**
     * 페이징, 필터링, 정렬이 적용된 프로젝트 조회
     *
     * @param projectIds 조회할 프로젝트 ID 리스트 (권한에 따라 필터링됨, null이면 전체)
     * @param startDate 계약 시작일 검색 범위 시작
     * @param endDate 계약 시작일 검색 범위 종료
     * @param status 프로젝트 상태
     * @param sortOrder 정렬 조건
     * @param cursor 커서
     * @param size 페이지 크기
     * @return 페이징된 프로젝트 목록
     */
    public List<Project> findProjectsWithPaging(List<Long> projectIds, LocalDate startDate, LocalDate endDate,
                                                Status status, ProjectListRequest.SortOrder sortOrder, Long cursor, int size) {

        return projectRepository.findProjectsWithPaging(projectIds, startDate, endDate,
                status, sortOrder, cursor, size);
    }
}
