package com.project.board0905.domain.comment.dto;

import com.project.board0905.domain.comment.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponse {
    private Long id;
    private Long boardId;
    private Long authorId;
    private String authorName;
    private Long parentId;
    private String content;
    private int depth;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommentResponse of(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .boardId(comment.getBoard().getId())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getUsername())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .content(comment.getContent())
                .depth(comment.getDepth())
                .deleted(comment.isDeleted())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
