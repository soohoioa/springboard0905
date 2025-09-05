package com.project.board0905.domain.search.service;

import com.project.board0905.domain.search.dto.BoardSearchCondition;
import com.project.board0905.domain.search.dto.BoardSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchService {
    Page<BoardSearchDto> searchBoards(BoardSearchCondition boardSearchCondition, Pageable pageable);
}