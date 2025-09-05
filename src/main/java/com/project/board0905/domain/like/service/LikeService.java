package com.project.board0905.domain.like.service;

import com.project.board0905.domain.like.dto.LikeResponse;
import com.project.board0905.domain.like.dto.LikeToggleResponse;

public interface LikeService {
    LikeResponse like(Long userId, Long boardId);
    void unlike(Long userId, Long boardId);
    LikeToggleResponse toggle(Long userId, Long boardId);
    long count(Long boardId);
    boolean isLiked(Long userId, Long boardId);
}
