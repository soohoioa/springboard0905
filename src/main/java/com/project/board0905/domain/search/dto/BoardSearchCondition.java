package com.project.board0905.domain.search.dto;

import com.project.board0905.domain.search.model.BoardSearchSort;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardSearchCondition {
    private final String keyword;
    private final Long authorId;
    private final String authorName;
    private final Integer categoryId;
    private final String categorySlug;
    private final Boolean noticeOnly;
    private final Boolean includeDeleted;
    private final Integer minLikes;
    private final Integer minComments;
    private final LocalDateTime from;
    private final LocalDateTime to;
    private final BoardSearchSort sort;
}
