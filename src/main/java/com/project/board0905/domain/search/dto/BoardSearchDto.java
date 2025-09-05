package com.project.board0905.domain.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BoardSearchDto {
    private final Long id;
    private final Long authorId;
    private final String authorName;
    private final Integer categoryId;
    private final String categoryName;
    private final String title;
    private final String snippet;     // 내용 일부
    private final Integer viewCount;
    private final boolean notice;
    private final boolean deleted;
    private final long likeCount;
    private final long commentCount;
    private final double relevance;   // 연관도 점수
    private final LocalDateTime createdAt;
}
