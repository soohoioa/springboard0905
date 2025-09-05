package com.project.board0905.domain.search.repository;

import com.project.board0905.domain.search.dto.BoardSearchCondition;
import com.project.board0905.domain.search.dto.BoardSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardSearchRepositoryCustom {
    Page<BoardSearchDto> search(BoardSearchCondition boardSearchCondition, Pageable pageable);
}
