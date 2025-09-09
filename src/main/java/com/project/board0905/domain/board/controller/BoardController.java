package com.project.board0905.domain.board.controller;

import com.project.board0905.common.web.CommonApiResponse;
import com.project.board0905.domain.board.dto.BoardCreateRequest;
import com.project.board0905.domain.board.dto.BoardResponse;
import com.project.board0905.domain.board.dto.BoardUpdateRequest;
import com.project.board0905.domain.board.service.BoardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/boards")
@Tag(name = "Board", description = "게시글 API")
public class BoardController {

    private final BoardService boardService;

    // 생성
    @PostMapping
    public ResponseEntity<CommonApiResponse<BoardResponse>> create(
            @AuthenticationPrincipal(expression = "userId") Long authorId, // Security 적용 안했으면 null로 옴
            @Valid @RequestBody BoardCreateRequest boardCreateRequest
    ) {
        BoardResponse boardResponse = boardService.create(authorId, boardCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonApiResponse.ok(boardResponse));
    }

    // 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<CommonApiResponse<BoardResponse>> get(@PathVariable Long id) {
        BoardResponse boardResponse = boardService.get(id);
        return ResponseEntity.ok(CommonApiResponse.ok(boardResponse));
    }

    // 페이지 조회
    @GetMapping
    public ResponseEntity<CommonApiResponse<Page<BoardResponse>>> page(Pageable pageable) {
        Page<BoardResponse> page = boardService.page(pageable);
        return ResponseEntity.ok(CommonApiResponse.ok(page));
    }

    // 수정
    @PatchMapping("/{id}")
    public ResponseEntity<CommonApiResponse<BoardResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody BoardUpdateRequest boardUpdateRequest
    ) {
        BoardResponse boardResponse = boardService.update(id, boardUpdateRequest);
        return ResponseEntity.ok(CommonApiResponse.ok(boardResponse));
    }

    // 삭제(소프트)
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonApiResponse<Void>> delete(@PathVariable Long id) {
        boardService.delete(id);
        return ResponseEntity.ok(CommonApiResponse.ok());
    }

    // 조회수 증가 (선택: 상세 호출 시 필터에서 처리 가능)
    @PostMapping("/{id}/views")
    public ResponseEntity<CommonApiResponse<Void>> increaseView(@PathVariable Long id) {
        boardService.increaseView(id);
        return ResponseEntity.ok(CommonApiResponse.ok());
    }
}
