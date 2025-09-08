package com.project.board0905.domain.search.repository;

import com.project.board0905.common.error.BusinessException;
import com.project.board0905.domain.search.dto.BoardSearchCondition;
import com.project.board0905.domain.search.dto.BoardSearchDto;
import com.project.board0905.domain.search.model.BoardSearchSort;
import com.project.board0905.domain.user.entity.QUser;
import com.project.board0905.domain.board.entity.QBoard;
import com.project.board0905.domain.category.entity.QCategory;
import com.project.board0905.domain.like.entity.QLike;
import com.project.board0905.domain.comment.entity.QComment;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.project.board0905.domain.search.error.SearchErrorCode.INVALID_DATE_RANGE;
import static com.project.board0905.domain.search.error.SearchErrorCode.UNSUPPORTED_SORT;

@Repository
@RequiredArgsConstructor
public class BoardSearchRepositoryImpl implements BoardSearchRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QBoard board = QBoard.board;
    private static final QUser user = QUser.user;
    private static final QCategory category = QCategory.category;
    private static final QLike like = QLike.like;
    private static final QComment comment = QComment.comment;

    @Override
    public Page<BoardSearchDto> search(BoardSearchCondition c, Pageable pageable) {

        // 유효성
        if (c.getFrom() != null && c.getTo() != null && c.getFrom().isAfter(c.getTo())) {
            throw new BusinessException(INVALID_DATE_RANGE);
        }

        // 서브쿼리: 좋아요/댓글 수
        NumberExpression<Long> likeCount = Expressions.numberTemplate(
                Long.class,
                "(select count(*) from likes l where l.board_id = {0})",
                board.id
        );

        NumberExpression<Long> commentCount = Expressions.numberTemplate(
                Long.class,
                "(select count(*) from comments c where c.board_id = {0} and c.is_deleted = 0)",
                board.id
        );

        // 연관도 점수 (제목 2, 내용 1 가중치)
        NumberExpression<Integer> relevance = Expressions.numberTemplate(
                Integer.class,
                " (case when lower({0}) like lower({1}) then 2 else 0 end) + " +
                        " (case when lower({2}) like lower({1}) then 1 else 0 end) ",
                board.title,
                keywordLikeParam(c.getKeyword()),
                board.content
        );

        // 본쿼리
        JPAQuery<BoardSearchDto> contentQuery = queryFactory
                .select(Projections.constructor(
                        BoardSearchDto.class,
                        board.id,
                        user.id,
                        user.username,
                        category.id,
                        category.name,
                        board.title,
                        snippetExpr(board.content, c.getKeyword()),
                        board.viewCount,
                        board.isNotice,
                        board.isDeleted,
                        likeCount.coalesce(0L),
                        commentCount.coalesce(0L),
                        relevance.coalesce(0),
                        board.createdAt
                ))
                .from(board)
                .join(board.author, user)
                .leftJoin(board.category, category)
                .where(
                        includeDeleted(c.getIncludeDeleted()),
                        eqAuthorId(c.getAuthorId()),
                        containsAuthorName(c.getAuthorName()),
                        eqCategory(c.getCategoryId(), c.getCategorySlug()),
                        dateBetween(c.getFrom(), c.getTo()),
                        keywordFilter(c.getKeyword()),
                        noticeOnly(c.getNoticeOnly())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifiers(c.getSort(), relevance, likeCount, commentCount));

        List<BoardSearchDto> content = contentQuery.fetch();

        // 카운트 쿼리 (가벼운 버전)
        JPAQuery<Long> countQuery = queryFactory
                .select(board.count())
                .from(board)
                .join(board.author, user)
                .leftJoin(board.category, category)
                .where(
                        includeDeleted(c.getIncludeDeleted()),
                        eqAuthorId(c.getAuthorId()),
                        containsAuthorName(c.getAuthorName()),
                        eqCategory(c.getCategoryId(), c.getCategorySlug()),
                        dateBetween(c.getFrom(), c.getTo()),
                        keywordFilter(c.getKeyword()),
                        noticeOnly(c.getNoticeOnly())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /* ================== WHERE helpers ================== */

    private BooleanExpression includeDeleted(Boolean includeDeleted) {
        if (includeDeleted == null || !includeDeleted) return board.isDeleted.isFalse();
        return null;
    }

    private BooleanExpression eqAuthorId(Long authorId) {
        return authorId == null ? null : user.id.eq(authorId);
    }

    private BooleanExpression containsAuthorName(String authorName) {
        return (authorName == null || authorName.isBlank()) ? null : user.username.containsIgnoreCase(authorName);
    }

    private BooleanExpression eqCategory(Integer categoryId, String slug) {
        if (categoryId != null) return category.id.eq(categoryId);
        if (slug != null && !slug.isBlank()) return category.slug.eq(slug);
        return null;
    }

    private BooleanExpression dateBetween(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) return board.createdAt.between(from, to);
        if (from != null) return board.createdAt.goe(from);
        if (to != null) return board.createdAt.loe(to);
        return null;
    }

    private BooleanExpression keywordFilter(String keyword) {
        return (keyword == null || keyword.isBlank()) ? null :
                board.title.containsIgnoreCase(keyword)
                        .or(board.content.containsIgnoreCase(keyword));
    }

    private BooleanExpression noticeOnly(Boolean noticeOnly) {
        if (noticeOnly == null || !noticeOnly) return null;
        return board.isNotice.isTrue();
    }

    /* ================== ORDER helpers ================== */

    private OrderSpecifier<?>[] orderSpecifiers(BoardSearchSort sort,
                                                NumberExpression<Integer> relevance,
                                                NumberExpression<Long> likeCount,
                                                NumberExpression<Long> commentCount) {

        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (sort == null) sort = BoardSearchSort.RECENT;

        switch (sort) {
            case RECENT -> orders.add(new OrderSpecifier<>(Order.DESC, board.createdAt));
            case OLDEST -> orders.add(new OrderSpecifier<>(Order.ASC, board.createdAt));
            case MOST_VIEWED -> {
                orders.add(new OrderSpecifier<>(Order.DESC, board.viewCount));
                orders.add(new OrderSpecifier<>(Order.DESC, board.createdAt));
            }
            case MOST_LIKED -> {
                orders.add(new OrderSpecifier<>(Order.DESC, likeCount));
                orders.add(new OrderSpecifier<>(Order.DESC, board.createdAt));
            }
            case MOST_COMMENTED -> {
                orders.add(new OrderSpecifier<>(Order.DESC, commentCount));
                orders.add(new OrderSpecifier<>(Order.DESC, board.createdAt));
            }
            case RELEVANCE -> {
                // 키워드 없으면 최신순으로 폴백
                orders.add(new OrderSpecifier<>(Order.DESC, relevance));
                orders.add(new OrderSpecifier<>(Order.DESC, board.createdAt));
            }
            default -> throw new BusinessException(UNSUPPORTED_SORT);
        }
        return orders.toArray(OrderSpecifier[]::new);
    }

    /* ================== 기타 helpers ================== */

    private StringTemplate snippetExpr(StringPath content, String keyword) {
        // DB 독립적인 간단 스니펫 (앞 200자)
        // Full-Text 사용 시 DB 함수로 대체 가능
        return Expressions.stringTemplate("substring({0}, 1, 200)", content);
    }

    private StringTemplate keywordLikeParam(String keyword) {
        String like = (keyword == null || keyword.isBlank()) ? "" : "%" + keyword + "%";
        return Expressions.stringTemplate("'"+ like.replace("'", "''") +"'");
    }
}
