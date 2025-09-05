package com.project.board0905.domain.search.model;

public enum BoardSearchSort {
    RECENT,          // 최신순
    OLDEST,          // 오래된순
    MOST_VIEWED,     // 조회수
    MOST_LIKED,      // 좋아요
    MOST_COMMENTED,  // 댓글수
    RELEVANCE        // 연관도 (제목>내용 가중치)
}
