package com.workhub.post.service.post;

import com.workhub.global.port.AuthorLookupPort;
import com.workhub.global.port.dto.AuthorProfile;
import com.workhub.post.dto.post.response.PostPageResponse;
import com.workhub.post.dto.post.response.PostResponse;
import com.workhub.post.dto.post.response.PostThreadResponse;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostFile;
import com.workhub.post.entity.PostLink;
import com.workhub.post.entity.PostType;
import com.workhub.post.service.PostValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadPostService {

    private final PostService postService;
    private final PostValidator postValidator;
    private final AuthorLookupPort authorLookupPort;
    /**
     * 프로젝트/노드 범위 내 게시글을 단건 조회한다.
     *
     * @param projectId 프로젝트 ID
     * @param nodeId 노드 ID
     * @param postId 게시글 ID
     * @return 조회된 게시글
     */
    public PostResponse findById(Long projectId, Long nodeId, Long postId) {
        postValidator.validateNodeAndProjectForRead(nodeId, projectId);
        Post post = postService.findById(postId);
        postService.validateNode(post, nodeId);

        List<PostFile> files = postService.findFilesByPostId(postId).stream()
                .filter(file -> file.getDeletedAt() == null)
                .toList();
        List<PostLink> links = postService.findLinksByPostId(postId).stream()
                .filter(link -> link.getDeletedAt() == null)
                .toList();

        String userName = authorLookupPort.findByUserId(post.getUserId())
                .map(AuthorProfile::userName)
                .orElse(null);

        return PostResponse.from(post, files, links, userName);
    }

    /**
     * 프로젝트/노드 범위 내 게시글 목록을 검색한다.
     *
     * @param projectId 프로젝트 ID
     * @param nodeId 노드 ID
     * @param keyword 검색 키워드
     * @param postType 게시글 타입
     * @param pageable 페이징 정보
     * @return 검색 결과 페이지
     */
    public PostPageResponse search(Long projectId,
                                   Long nodeId,
                                   String keyword,
                                   PostType postType,
                                   Pageable pageable) {
        postValidator.validateNodeAndProjectForRead(nodeId, projectId);

        Page<Post> parentPage = postService.searchParentPosts(nodeId, keyword, postType, pageable);

        List<Long> parentIds = parentPage.getContent().stream().map(Post::getPostId).toList();
        Map<Long, List<Post>> childrenMap = buildChildrenMap(parentIds);
        Map<Long, AuthorProfile> authorMap = loadAuthors(parentPage.getContent(), childrenMap);

        List<PostThreadResponse> threads = parentPage.getContent().stream()
                .map(post -> toThreadResponse(post, childrenMap, authorMap))
                .toList();

        return PostPageResponse.of(threads, parentPage);
    }

    /**
     * 부모-자식 관계를 빠르게 탐색할 수 있도록 부모 ID를 키로 하는 children map을 만든다.
     *
     * @param rootIds 최상위 게시글 ID 목록
     * @return 부모 ID -> 자식 게시글 목록 매핑
     */
    private Map<Long, List<Post>> buildChildrenMap(List<Long> rootIds) {
        Map<Long, List<Post>> childrenMap = new java.util.HashMap<>();
        List<Long> currentLevel = rootIds;

        while (!currentLevel.isEmpty()) {
            List<Post> children = postService.findChildren(currentLevel);
            if (children.isEmpty()) {
                break;
            }
            for (Post child : children) {
                childrenMap.computeIfAbsent(child.getParentPostId(), key -> new ArrayList<>()).add(child);
            }
            currentLevel = children.stream().map(Post::getPostId).toList();
        }

        return childrenMap;
    }

    /**
     * 게시글과 하위 댓글을 PostThreadResponse 구조로 재귀 변환한다.
     *
     * @param post 현재 게시글
     * @param childrenMap 부모-자식 매핑
     * @return 스레드 응답
     */
    private PostThreadResponse toThreadResponse(Post post,
                                                Map<Long, List<Post>> childrenMap,
                                                Map<Long, AuthorProfile> authorMap) {
        String userName = Optional.ofNullable(authorMap.get(post.getUserId()))
                .map(AuthorProfile::userName)
                .orElse(null);

        List<PostThreadResponse> replies = childrenMap.getOrDefault(post.getPostId(), List.of()).stream()
                .map(child -> toThreadResponse(child, childrenMap, authorMap))
                .toList();
        return PostThreadResponse.from(post, replies, userName);
    }

    private Map<Long, AuthorProfile> loadAuthors(List<Post> parents, Map<Long, List<Post>> childrenMap) {
        Set<Long> userIds = new HashSet<>();
        parents.forEach(post -> {
            if (post.getUserId() != null) {
                userIds.add(post.getUserId());
            }
        });
        childrenMap.values().forEach(children ->
                children.forEach(child -> {
                    if (child.getUserId() != null) {
                        userIds.add(child.getUserId());
                    }
                })
        );

        if (userIds.isEmpty()) {
            return Map.of();
        }
        return authorLookupPort.findByUserIds(new ArrayList<>(userIds));
    }
}
