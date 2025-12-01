package com.workhub.cs.service;

import com.workhub.cs.dto.CsPostResponse;
import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsPostStatus;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.Status;
import com.workhub.project.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadCsPostServiceTest {

    @Mock
    private CsPostService csPostService;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ReadCsPostService readCsPostService;

    private CsPost mockSaved;

    @BeforeEach
    void init() {
        mockSaved = CsPost.builder()
                .csPostId(1L)
                .projectId(1L)
                .userId(2L)
                .title("문의 제목")
                .content("문의 내용")
                .csPostStatus(CsPostStatus.RECEIVED)
                .build();
    }

    @Test
    @DisplayName("CS POST 게시글 상세 조회한다.")
    void givenCsPostId_whenGetCsPost_thenSuccess() {
        Long csPostId = 1L;
        Long projectId = 1L;

        when(projectService.validateCompletedProject(projectId)).thenReturn(
                Project.builder()
                        .projectId(projectId)
                        .projectTitle("project")
                        .status(Status.COMPLETED)
                        .build()
        );
        when(csPostService.findById(csPostId)).thenReturn(mockSaved);
        when(csPostService.findFilesByCsPostId(csPostId)).thenReturn(List.of());

        CsPostResponse response = readCsPostService.findCsPost(projectId, csPostId);

        verify(projectService).validateCompletedProject(projectId);
        verify(csPostService).findById(csPostId);
        verify(csPostService).findFilesByCsPostId(csPostId);
        assertThat(response.content()).isEqualTo("문의 내용");
        assertThat(response.title()).isEqualTo("문의 제목");
    }

    @Test
    @DisplayName("다른 프로젝트의 CS POST 조회 시 예외 발생")
    void givenWrongProjectId_whenGetCsPost_thenThrow() {
        Long csPostId = 1L;
        Long projectId = 999L;

        when(projectService.validateCompletedProject(projectId)).thenReturn(
                Project.builder()
                        .projectId(projectId)
                        .projectTitle("project")
                        .status(Status.COMPLETED)
                        .build()
        );
        CsPost otherProjectPost = CsPost.builder()
                .csPostId(csPostId)
                .projectId(1L)
                .userId(1L)
                .title("문의 제목")
                .content("문의 내용")
                .csPostStatus(CsPostStatus.RECEIVED)
                .build();
        when(csPostService.findById(csPostId)).thenReturn(otherProjectPost);

        assertThatThrownBy(() -> readCsPostService.findCsPost(projectId, csPostId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.NOT_MATCHED_PROJECT_CS_POST.getMessage());

        verify(projectService).validateCompletedProject(projectId);
        verify(csPostService).findById(csPostId);
        verify(csPostService, never()).findFilesByCsPostId(csPostId);
    }
}
