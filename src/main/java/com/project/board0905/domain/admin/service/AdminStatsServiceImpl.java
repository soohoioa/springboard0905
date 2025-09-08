package com.project.board0905.domain.admin.service;

import com.project.board0905.common.error.BusinessException;
import com.project.board0905.domain.admin.error.AdminStatsErrorCode;
import com.project.board0905.domain.admin.model.AdminOverviewStats;
import com.project.board0905.domain.board.entity.QBoard;
import com.project.board0905.domain.category.entity.QCategory;
import com.project.board0905.domain.comment.entity.QComment;
import com.project.board0905.domain.like.entity.QLike;
import com.project.board0905.domain.user.entity.QUser;
import com.project.board0905.domain.user.entity.UserStatus;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.board0905.domain.admin.model.AdminOverviewStats.TimeSeriesPoint;
import com.project.board0905.domain.admin.model.AdminOverviewStats.BoardRank;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsServiceImpl implements AdminStatsService {

    private final JPAQueryFactory queryFactory;

    private static final QBoard board = QBoard.board;
    private static final QUser user = QUser.user;
    private static final QCategory category = QCategory.category;
    private static final QLike like = QLike.like;
    private static final QComment comment = QComment.comment;

    @Override
    public AdminOverviewStats overview(LocalDate from, LocalDate to, int topN) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException(AdminStatsErrorCode.INVALID_DATE_RANGE);
        }

        LocalDateTime fromDt = (from == null) ? LocalDate.now().minusDays(6).atStartOfDay() : from.atStartOfDay();
        LocalDateTime toDt = (to == null) ? LocalDate.now().plusDays(1).atStartOfDay() : to.plusDays(1).atStartOfDay();

        // 누적/현재
        long totalUsers = queryFactory.select(user.count()).from(user).fetchOne();
        long activeUsers = queryFactory.select(user.count()).from(user).where(user.status.eq(UserStatus.ACTIVE)).fetchOne();
        long suspendedUsers = queryFactory.select(user.count()).from(user).where(user.status.eq(UserStatus.SUSPENDED)).fetchOne();
        long deletedUsers = queryFactory.select(user.count()).from(user).where(user.status.eq(UserStatus.DELETED)).fetchOne();

        long totalBoards = queryFactory.select(board.count()).from(board).fetchOne();
        long deletedBoards = queryFactory.select(board.count()).from(board).where(board.isDeleted.isTrue()).fetchOne();
        long noticeBoards = queryFactory.select(board.count()).from(board).where(board.isNotice.isTrue()).fetchOne();

        long totalComments = queryFactory.select(comment.count()).from(comment).fetchOne();
        long totalLikes = queryFactory.select(like.count()).from(like).fetchOne();

        // 시계열 (일 단위)
        StringTemplate dayOfUser = Expressions.stringTemplate("date({0})", user.createdAt);
        List<TimeSeriesPoint> usersByDay = queryFactory
                .select(Projections.bean(TimeSeriesPoint.class,
                        Expressions.dateTemplate(java.time.LocalDate.class, "date({0})", user.createdAt).as("date"),
                        user.id.count().as("count")))
                .from(user)
                .where(user.createdAt.goe(fromDt).and(user.createdAt.lt(toDt)))
                .groupBy(dayOfUser)
                .orderBy(Expressions.dateTemplate(java.time.LocalDate.class, "date({0})", user.createdAt).asc())
                .fetch();

        StringTemplate dayOfBoard = Expressions.stringTemplate("date({0})", board.createdAt);
        List<TimeSeriesPoint> boardsByDay = queryFactory
                .select(Projections.bean(TimeSeriesPoint.class,
                        Expressions.dateTemplate(java.time.LocalDate.class, "date({0})", board.createdAt).as("date"),
                        board.id.count().as("count")))
                .from(board)
                .where(board.createdAt.goe(fromDt).and(board.createdAt.lt(toDt)))
                .groupBy(dayOfBoard)
                .orderBy(Expressions.dateTemplate(java.time.LocalDate.class, "date({0})", board.createdAt).asc())
                .fetch();

        // 랭킹
        List<BoardRank> topByViews = queryFactory
                .select(Projections.bean(BoardRank.class,
                        board.id.as("boardId"),
                        board.title.as("title"),
                        user.username.as("authorName"),
                        category.name.as("categoryName"),
                        board.viewCount.as("viewCount"),
                        ExpressionUtils.as(Expressions.constant(0L), "likeCount"),
                        ExpressionUtils.as(Expressions.constant(0L), "commentCount")))
                .from(board)
                .join(board.author, user)
                .leftJoin(board.category, category)
                .where(board.isDeleted.isFalse())
                .orderBy(board.viewCount.desc(), board.createdAt.desc())
                .limit(topN)
                .fetch();

        List<BoardRank> topByLikes = queryFactory
                .select(Projections.bean(BoardRank.class,
                        board.id.as("boardId"),
                        board.title.as("title"),
                        user.username.as("authorName"),
                        category.name.as("categoryName"),
                        board.viewCount.as("viewCount"),
                        like.id.countDistinct().as("likeCount"),
                        ExpressionUtils.as(Expressions.constant(0L), "commentCount")))
                .from(board)
                .join(board.author, user)
                .leftJoin(board.category, category)
                .leftJoin(like).on(like.board.id.eq(board.id))
                .where(board.isDeleted.isFalse())
                .groupBy(board.id, board.title, user.username, category.name, board.viewCount)
                .orderBy(like.id.countDistinct().desc(), board.createdAt.desc())
                .limit(topN)
                .fetch();

        List<BoardRank> topByComments = queryFactory
                .select(Projections.bean(BoardRank.class,
                        board.id.as("boardId"),
                        board.title.as("title"),
                        user.username.as("authorName"),
                        category.name.as("categoryName"),
                        board.viewCount.as("viewCount"),
                        ExpressionUtils.as(Expressions.constant(0L), "likeCount"),
                        comment.id.countDistinct().as("commentCount")))
                .from(board)
                .join(board.author, user)
                .leftJoin(board.category, category)
                .leftJoin(comment).on(comment.board.id.eq(board.id).and(comment.isDeleted.isFalse()))
                .where(board.isDeleted.isFalse())
                .groupBy(board.id, board.title, user.username, category.name, board.viewCount)
                .orderBy(comment.id.countDistinct().desc(), board.createdAt.desc())
                .limit(topN)
                .fetch();

        return AdminOverviewStats.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .suspendedUsers(suspendedUsers)
                .deletedUsers(deletedUsers)
                .totalBoards(totalBoards)
                .deletedBoards(deletedBoards)
                .noticeBoards(noticeBoards)
                .totalComments(totalComments)
                .totalLikes(totalLikes)
                .usersByDay(usersByDay)
                .boardsByDay(boardsByDay)
                .topByViews(topByViews)
                .topByLikes(topByLikes)
                .topByComments(topByComments)
                .build();
    }

}
