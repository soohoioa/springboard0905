package com.project.board0905.domain.like.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeToggleResponse {
    private final boolean liked;  // true면 방금 좋아요됨, false면 좋아요 해제됨
    private final long likeCount; // 현재 게시글 총 좋아요 수
}
