package com.workhub.post.repository.post;

import com.workhub.post.entity.PostFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostFileRepository extends JpaRepository<PostFile, Long> {

    List<PostFile> findByPostId(Long postId);
}
