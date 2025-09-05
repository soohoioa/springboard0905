package com.project.board0905.domain.like.dto;

import com.project.board0905.domain.like.entity.Like;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LikeResponse {
    private Long id;
    private Long boardId;
    private Long userId;
    private LocalDateTime createdAt;

    public static LikeResponse of(Like like) {
        return LikeResponse.builder()
                .id(like.getId())
                .boardId(like.getBoard().getId())
                .userId(like.getUser().getId())
                .createdAt(like.getCreatedAt())
                .build();
    }
}
