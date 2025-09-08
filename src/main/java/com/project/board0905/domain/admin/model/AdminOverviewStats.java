package com.project.board0905.domain.admin.model;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class AdminOverviewStats {

    // 누적/현재
    private final long totalUsers;
    private final long activeUsers;
    private final long suspendedUsers;
    private final long deletedUsers;

    private final long totalBoards;
    private final long deletedBoards;
    private final long noticeBoards;

    private final long totalComments;
    private final long totalLikes;

    // 시계열
    private final List<TimeSeriesPoint> usersByDay;
    private final List<TimeSeriesPoint> boardsByDay;

    // 랭킹
    private final List<BoardRank> topByViews;
    private final List<BoardRank> topByLikes;
    private final List<BoardRank> topByComments;

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSeriesPoint {
        private LocalDate date;
        private long count;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class BoardRank {
        private Long boardId;
        private String title;
        private String authorName;
        private String categoryName;
        private int viewCount;
        private long likeCount;
        private long commentCount;
    }
}
