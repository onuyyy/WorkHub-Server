package com.workhub.cs.service;

import com.workhub.cs.dto.CsPostFileRequest;
import com.workhub.cs.dto.CsPostRequest;
import com.workhub.cs.dto.CsPostResponse;
import com.workhub.cs.dto.CsPostUpdateRequest;
import com.workhub.cs.entity.CsPost;
import com.workhub.cs.repository.CsPostFileRepository;
import com.workhub.cs.repository.CsPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CsPostServiceTest {

    @Mock
    private CsPostRepository csPostRepository;

    @Mock
    private CsPostFileRepository csPostFileRepository;

    @InjectMocks
    private CsPostService csPostService;

    private CsPost mockSaved;

    @BeforeEach
    public void init(){
        mockSaved = CsPost.builder()
                .csPostId(1L)
                .projectId(1L)
                .userId(2L)
                .title("문의 제목")
                .content("문의 내용")
                .build();
    }

    @Test
    @DisplayName(" CS 게시글을 작성하면 작성한 게시글 정보를 보여준다.")
    void givenCsPostCreateRequest_whenCreateCsPost_thenSuccess() {
        // given
        Long projectId = 1L;
        CsPostRequest request = new CsPostRequest("문의 제목", "문의 내용", null);

        when(csPostRepository.save(any(CsPost.class)))
                .thenReturn(mockSaved);

        // when
        CsPostResponse result = csPostService.create(projectId, request);

        // then
        assertThat(result.csPostId()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("문의 제목");
        assertThat(result.content()).isEqualTo("문의 내용");

        verify(csPostRepository).save(any(CsPost.class));
        verify(csPostFileRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("파일이 포함된 게시글 작성 시 파일도 저장된다.")
    void givenRequestWithFiles_whenCreate_thenFilesAreSaved() {
        // given
        Long projectId = 1L;

        List<CsPostFileRequest> fileRequests = Arrays.asList(
                new CsPostFileRequest("url1", "file1", 1),
                new CsPostFileRequest("url2", "file2", 2)
        );

        CsPostRequest request =
                new CsPostRequest("문의 제목", "내용", fileRequests);

        when(csPostRepository.save(any(CsPost.class)))
                .thenReturn(mockSaved);

        // when
        csPostService.create(projectId, request);

        // then
        verify(csPostRepository).save(any(CsPost.class));
        verify(csPostFileRepository, times(1)).saveAll(anyList()); // saveAll만 호출됨
    }

    // todo : 프로젝트 밸리데이터 붙이고 나서 테스트 해야 함
    @Test
    @DisplayName("프로젝트가 존재하지 않으면 ProjectNotFoundException이 발생한다.")
    void givenInvalidProject_whenCreate_thenThrowProjectNotFound() {

    }

    @Test
    @DisplayName("요청 DTO가 매핑되어 엔티티로 저장되는지 검증한다.")
    void givenRequest_whenCreate_thenEntityMappedSuccessfully() {
        // given
        Long projectId = 1L;
        Long userId = 2L;

        CsPostRequest request = new CsPostRequest("문의 제목", "문의 내용", null);
        when(csPostRepository.save(any(CsPost.class)))
                .thenReturn(mockSaved);

        // when
        csPostService.create(projectId, request);

        // then
        verify(csPostRepository).save(argThat(post ->
                post.getProjectId().equals(projectId) &&
                        // post.getUserId().equals(userId) && // todo : security 적용 전이라 주석
                        post.getTitle().equals("문의 제목") &&
                        post.getContent().equals("문의 내용")
        ));
    }

    @Test
    @DisplayName("CS 게시글 수정 시 정상 저장되는지 검증한다.")
    void givenCsPostUpdateRequest_whenUpdate_thenSuccess() {

        // given
        Long projectId = 1L;
        Long csPostId = 2L; // 수정된 글 번호

        CsPost original = CsPost.builder()
                .csPostId(csPostId)
                .projectId(projectId)
                .title("원래 제목")
                .content("원래 내용")
                .build();

        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusMinutes(1);
        ReflectionTestUtils.setField(original, "updatedAt", originalUpdatedAt);

        CsPostUpdateRequest request =
                new CsPostUpdateRequest("수정 제목", "수정 완료", List.of());

        CsPost updated = CsPost.builder()
                .csPostId(csPostId)
                .projectId(projectId)
                .title("수정 제목")
                .content("수정 완료")
                .build();

        when(csPostRepository.findById(csPostId))
                .thenReturn(Optional.of(original));

        when(csPostRepository.save(any(CsPost.class)))
                .thenReturn(updated);

        ArgumentCaptor<CsPost> captor = ArgumentCaptor.forClass(CsPost.class);

        // when
        CsPostResponse result = csPostService.update(projectId, csPostId, request);

        // then
        assertThat(result.title()).isEqualTo("수정 제목");
        assertThat(result.content()).isEqualTo("수정 완료");

        verify(csPostRepository).save(captor.capture());
        CsPost savedEntity = captor.getValue();

        assertThat(savedEntity.getTitle()).isEqualTo("수정 제목");
        assertThat(savedEntity.getContent()).isEqualTo("수정 완료");

        verify(csPostRepository).findById(csPostId);
    }
}
