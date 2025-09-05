package com.project.board0905.domain.search.service;

import com.project.board0905.common.error.BusinessException;
import com.project.board0905.domain.search.dto.BoardSearchCondition;
import com.project.board0905.domain.search.dto.BoardSearchDto;
import com.project.board0905.domain.search.dto.BoardSearchRequest;
import com.project.board0905.domain.search.repository.BoardSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.project.board0905.domain.search.error.SearchErrorCode.INVALID_DATE_RANGE;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final BoardSearchRepository boardSearchRepository;

    @Override
    public Page<BoardSearchDto> searchBoards(BoardSearchCondition boardSearchCondition, Pageable pageable) {
        if (boardSearchCondition.getFrom() != null && boardSearchCondition.getTo() != null &&
                boardSearchCondition.getFrom().isAfter(boardSearchCondition.getTo())) {
            throw new BusinessException(INVALID_DATE_RANGE);
        }
        return boardSearchRepository.search(boardSearchCondition, pageable);
    }

    /* 편의: Controller에서 Request -> Condition 변환 */
    public static BoardSearchCondition from(BoardSearchRequest boardSearchRequest) {
        return BoardSearchCondition.builder()
                .keyword(boardSearchRequest.getQ())
                .authorId(boardSearchRequest.getAuthorId())
                .authorName(boardSearchRequest.getAuthorName())
                .categoryId(boardSearchRequest.getCategoryId())
                .categorySlug(boardSearchRequest.getCategorySlug())
                .noticeOnly(boardSearchRequest.getNoticeOnly())
                .includeDeleted(boardSearchRequest.getIncludeDeleted())
                .minLikes(boardSearchRequest.getMinLikes())
                .minComments(boardSearchRequest.getMinComments())
                .from(boardSearchRequest.getFrom())
                .to(boardSearchRequest.getTo())
                .sort(boardSearchRequest.getSort())
                .build();
    }
}

