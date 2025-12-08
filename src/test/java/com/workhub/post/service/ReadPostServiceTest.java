package com.workhub.post.service;

import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostType;
import com.workhub.post.dto.post.response.PostResponse;
import com.workhub.post.repository.post.PostFileRepository;
import com.workhub.post.repository.post.PostLinkRepository;
import com.workhub.post.repository.post.PostRepository;
import com.workhub.post.service.post.PostService;
import com.workhub.post.service.post.ReadPostService;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.Status;
import com.workhub.project.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
class ReadPostServiceTest {

    @Mock
    PostRepository postRepository;
    @Mock
    PostFileRepository postFileRepository;
    @Mock
    PostLinkRepository postLinkRepository;
    @InjectMocks
    PostService postService;
    @Mock
    ProjectService projectService;

    ReadPostService readPostService;

    @BeforeEach
    void setUp() {
        readPostService = new ReadPostService(postService, projectService);
        given(postRepository.findByParentPostIdAndDeletedAtIsNull(anyLong())).willReturn(Collections.emptyList());
        given(postFileRepository.findByPostId(anyLong())).willReturn(Collections.emptyList());
        given(postLinkRepository.findByPostId(anyLong())).willReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("게시글을 단건 조회하면 파일/링크가 필터링되어 응답된다")
    void findById_returnsPostWithFilesAndLinks() {
        Post post = Post.builder()
                .postId(1L)
                .title("title")
                .content("content")
                .type(PostType.NOTICE)
                .projectNodeId(20L)
                .userId(30L)
                .build();
        given(projectService.validateProject(10L)).willReturn(mockProject(10L));
        given(postRepository.findByPostIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(post));

        PostResponse response = readPostService.findById(10L, 20L, 1L);

        assertThat(response.postId()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("title");
    }

    private Project mockProject(Long projectId) {
        return Project.builder()
                .projectId(projectId)
                .projectTitle("project")
                .status(Status.IN_PROGRESS)
                .build();
    }
}
