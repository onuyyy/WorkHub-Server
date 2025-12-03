package com.workhub.cs.service.csPost;

import com.workhub.cs.entity.CsPost;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteCsPostServiceTest {

    @Mock
    private CsPostService csPostService;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private DeleteCsPostService deleteCsPostService;

    private CsPost mockSaved;

    @BeforeEach
    void init() {
        mockSaved = CsPost.builder()
                .csPostId(1L)
                .projectId(1L)
                .userId(2L)
                .title("문의 제목")
                .content("문의 내용")
                .build();
    }

    @Test
    @DisplayName("CS POST 게시글이 정상적으로 삭제된다.")
    void givenDeleteCsPost_whenDelete_thenSuccess() {
        Long projectId = 1L;
        Long csPostId = 1L;

        mockProjectLookup(projectId);
        when(csPostService.findById(csPostId)).thenReturn(mockSaved);

        deleteCsPostService.delete(projectId, csPostId);

        assertThat(mockSaved.getDeletedAt()).isNotNull();
        verify(projectService).validateCompletedProject(projectId);
        verify(csPostService).findById(csPostId);
    }

    @Test
    @DisplayName("CS POST 게시글 삭제시 예외 처리")
    void givenNotExistsCsPost_whenDelete_thenThrowNotFound() {
        Long projectId = 1L;
        Long csPostId = 1L;

        mockProjectLookup(projectId);
        when(csPostService.findById(csPostId)).thenThrow(new BusinessException(ErrorCode.NOT_EXISTS_CS_POST));

        assertThatThrownBy(() -> deleteCsPostService.delete(projectId, csPostId))
                .isInstanceOf(BusinessException.class);

        verify(projectService).validateCompletedProject(projectId);
    }

    @Test
    @DisplayName("이미 삭제된 게시글 삭제 시 예외 발생")
    void givenAlreadyDeletedPost_whenDelete_thenThrow() {
        Long projectId = 1L;
        Long csPostId = 1L;

        mockProjectLookup(projectId);
        mockSaved.markDeleted();
        when(csPostService.findById(csPostId)).thenReturn(mockSaved);

        assertThatThrownBy(() -> deleteCsPostService.delete(projectId, csPostId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_DELETED_CS_POST);

        verify(projectService).validateCompletedProject(projectId);
    }

    private void mockProjectLookup(Long projectId) {
        when(projectService.validateCompletedProject(projectId)).thenReturn(
                Project.builder()
                        .projectId(projectId)
                        .projectTitle("project")
                        .status(Status.COMPLETED)
                        .build()
        );
    }
}
