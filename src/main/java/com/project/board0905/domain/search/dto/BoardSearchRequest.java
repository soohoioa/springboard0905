package com.project.board0905.domain.search.dto;

import com.project.board0905.domain.search.model.BoardSearchSort;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class BoardSearchRequest {

    private String q;                // 키워드 (제목/내용)
    private Long authorId;           // 작성자 ID
    private String authorName;       // 작성자명 (부분일치)
    private Integer categoryId;      // 카테고리 ID
    private String categorySlug;     // 카테고리 슬러그
    private Boolean noticeOnly;      // 공지만
    private Boolean includeDeleted;  // 삭제글 포함

    private Integer minLikes;        // 최소 좋아요
    private Integer minComments;     // 최소 댓글수

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;      // 작성일 범위 시작
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;        // 작성일 범위 끝

    private BoardSearchSort sort = BoardSearchSort.RECENT;
}
