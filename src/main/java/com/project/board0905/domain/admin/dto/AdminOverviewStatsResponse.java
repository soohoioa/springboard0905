package com.project.board0905.domain.admin.dto;

import com.project.board0905.domain.admin.model.AdminOverviewStats;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter @Builder
public class AdminOverviewStatsResponse {

    private long totalUsers;
    private long activeUsers;
    private long suspendedUsers;
    private long deletedUsers;

    private long totalBoards;
    private long deletedBoards;
    private long noticeBoards;

    private long totalComments;
    private long totalLikes;

    private List<TimeSeriesPoint> usersByDay;
    private List<TimeSeriesPoint> boardsByDay;

    private List<BoardRankDto> topByViews;
    private List<BoardRankDto> topByLikes;
    private List<BoardRankDto> topByComments;

    @Getter @Builder
    public static class TimeSeriesPoint {
        private LocalDate date;
        private long count;
    }

    @Getter @Builder
    public static class BoardRankDto {
        private Long boardId;
        private String title;
        private String authorName;
        private String categoryName;
        private int viewCount;
        private long likeCount;
        private long commentCount;
    }

    public static AdminOverviewStatsResponse from(AdminOverviewStats adminOverviewStats) {
        return AdminOverviewStatsResponse.builder()
                .totalUsers(adminOverviewStats.getTotalUsers())
                .activeUsers(adminOverviewStats.getActiveUsers())
                .suspendedUsers(adminOverviewStats.getSuspendedUsers())
                .deletedUsers(adminOverviewStats.getDeletedUsers())
                .totalBoards(adminOverviewStats.getTotalBoards())
                .deletedBoards(adminOverviewStats.getDeletedBoards())
                .noticeBoards(adminOverviewStats.getNoticeBoards())
                .totalComments(adminOverviewStats.getTotalComments())
                .totalLikes(adminOverviewStats.getTotalLikes())
                .usersByDay(adminOverviewStats.getUsersByDay().stream().map(p ->
                        TimeSeriesPoint.builder().date(p.getDate()).count(p.getCount()).build()).toList())
                .boardsByDay(adminOverviewStats.getBoardsByDay().stream().map(p ->
                        TimeSeriesPoint.builder().date(p.getDate()).count(p.getCount()).build()).toList())
                .topByViews(adminOverviewStats.getTopByViews().stream().map(r ->
                        BoardRankDto.builder()
                                .boardId(r.getBoardId())
                                .title(r.getTitle())
                                .authorName(r.getAuthorName())
                                .categoryName(r.getCategoryName())
                                .viewCount(r.getViewCount())
                                .likeCount(r.getLikeCount())
                                .commentCount(r.getCommentCount())
                                .build()).toList())
                .topByLikes(adminOverviewStats.getTopByLikes().stream().map(r ->
                        BoardRankDto.builder()
                                .boardId(r.getBoardId())
                                .title(r.getTitle())
                                .authorName(r.getAuthorName())
                                .categoryName(r.getCategoryName())
                                .viewCount(r.getViewCount())
                                .likeCount(r.getLikeCount())
                                .commentCount(r.getCommentCount())
                                .build()).toList())
                .topByComments(adminOverviewStats.getTopByComments().stream().map(r ->
                        BoardRankDto.builder()
                                .boardId(r.getBoardId())
                                .title(r.getTitle())
                                .authorName(r.getAuthorName())
                                .categoryName(r.getCategoryName())
                                .viewCount(r.getViewCount())
                                .likeCount(r.getLikeCount())
                                .commentCount(r.getCommentCount())
                                .build()).toList())
                .build();
    }
}
