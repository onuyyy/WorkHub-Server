package com.workhub.post.repository;

import com.workhub.global.repository.BaseHistoryRepository;
import com.workhub.post.entity.PostHistory;
import org.springframework.stereotype.Repository;

@Repository
public interface PostHistoryRepository extends BaseHistoryRepository<PostHistory> {
}
