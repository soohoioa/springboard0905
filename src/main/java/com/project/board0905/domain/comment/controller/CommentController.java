package com.project.board0905.domain.comment.controller;

import com.project.board0905.common.web.CommonApiResponse;
import com.project.board0905.domain.comment.dto.CommentCreateRequest;
import com.project.board0905.domain.comment.dto.CommentResponse;
import com.project.board0905.domain.comment.dto.CommentUpdateRequest;
import com.project.board0905.domain.comment.service.CommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/boards/{boardId}/comments")
@Tag(name = "Comment", description = "답글 API")
public class CommentController {

    private final CommentService commentService;

    // 생성 (루트/대댓글 공용)
    @PostMapping
    public ResponseEntity<CommonApiResponse<CommentResponse>> create(
            @PathVariable Long boardId,
            @AuthenticationPrincipal(expression = "userId") Long authorId,
            @Valid @RequestBody CommentCreateRequest commentCreateRequest
    ) {
        commentCreateRequest.setBoardId(boardId);
        CommentResponse commentResponse = commentService.create(authorId, commentCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonApiResponse.ok(commentResponse));
    }

    // 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<CommonApiResponse<CommentResponse>> get(
            @PathVariable Long boardId, @PathVariable Long id
    ) {
        CommentResponse commentResponse = commentService.get(id); // boardId 검증은 service에서 parent/board 일치로 커버
        return ResponseEntity.ok(CommonApiResponse.ok(commentResponse));
    }

    // 페이지 조회
    @GetMapping
    public ResponseEntity<CommonApiResponse<Page<CommentResponse>>> page(
            @PathVariable Long boardId, Pageable pageable
    ) {
        Page<CommentResponse> page = commentService.pageByBoard(boardId, pageable);
        return ResponseEntity.ok(CommonApiResponse.ok(page));
    }

    // 전체 정렬 리스트 (createdAt ASC)
    @GetMapping("/all")
    public ResponseEntity<CommonApiResponse<List<CommentResponse>>> list(
            @PathVariable Long boardId
    ) {
        List<CommentResponse> list = commentService.listByBoard(boardId);
        return ResponseEntity.ok(CommonApiResponse.ok(list));
    }

    // 수정
    @PatchMapping("/{id}")
    public ResponseEntity<CommonApiResponse<CommentResponse>> update(
            @PathVariable Long boardId, @PathVariable Long id,
            @AuthenticationPrincipal(expression = "userId") Long authorId,
            @Valid @RequestBody CommentUpdateRequest commentUpdateRequest
    ) {
        CommentResponse commentResponse = commentService.update(id, commentUpdateRequest, authorId);
        return ResponseEntity.ok(CommonApiResponse.ok(commentResponse));
    }

    // 삭제(소프트)
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonApiResponse<Void>> delete(
            @PathVariable Long boardId, @PathVariable Long id,
            @AuthenticationPrincipal(expression = "userId") Long authorId
    ) {
        commentService.delete(id, authorId);
        return ResponseEntity.ok(CommonApiResponse.ok());
    }
}
