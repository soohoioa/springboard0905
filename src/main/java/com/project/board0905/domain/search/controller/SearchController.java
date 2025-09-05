package com.project.board0905.domain.search.controller;

import com.project.board0905.common.web.CommonApiResponse;
import com.project.board0905.domain.search.dto.BoardSearchCondition;
import com.project.board0905.domain.search.dto.BoardSearchDto;
import com.project.board0905.domain.search.dto.BoardSearchRequest;
import com.project.board0905.domain.search.service.SearchService;
import com.project.board0905.domain.search.service.SearchServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/boards")
    public ResponseEntity<CommonApiResponse<Page<BoardSearchDto>>> searchBoards(
            @Valid @ModelAttribute BoardSearchRequest boardSearchRequest, Pageable pageable
    ) {
        BoardSearchCondition cond = SearchServiceImpl.from(boardSearchRequest);
        Page<BoardSearchDto> page = searchService.searchBoards(cond, pageable);
        return ResponseEntity.ok(CommonApiResponse.ok(page));
    }
}
