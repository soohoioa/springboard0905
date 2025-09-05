package com.project.board0905.domain.board.dto;

import com.project.board0905.domain.board.entity.Board;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardResponse {
    private Long id;
    private Long authorId;
    private String authorName;
    private Integer categoryId;
    private String categoryName;
    private String title;
    private String content;
    private Integer viewCount;
    private boolean notice;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BoardResponse of(Board board) {
        return BoardResponse.builder()
                .id(board.getId())
                .authorId(board.getAuthor() != null ? board.getAuthor().getId() : null)
                .authorName(board.getAuthor() != null ? board.getAuthor().getUsername() : null)
                .categoryId(board.getCategory() != null ? board.getCategory().getId() : null)
                .categoryName(board.getCategory() != null ? board.getCategory().getName() : null)
                .title(board.getTitle())
                .content(board.getContent())
                .viewCount(board.getViewCount())
                .notice(board.isNotice())
                .deleted(board.isDeleted())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }
}
