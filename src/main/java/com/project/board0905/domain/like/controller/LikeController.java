package com.project.board0905.domain.like.controller;

import com.project.board0905.common.web.CommonApiResponse;
import com.project.board0905.domain.like.dto.LikeResponse;
import com.project.board0905.domain.like.dto.LikeToggleResponse;
import com.project.board0905.domain.like.service.LikeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/boards/{boardId}/likes")
@Tag(name = "Like", description = "좋아요 API")
public class LikeController {

    private final LikeService likeService;

    // 좋아요 추가
    @PostMapping
    public ResponseEntity<CommonApiResponse<LikeResponse>> like(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long boardId
    ) {
        LikeResponse likeResponse = likeService.like(userId, boardId);
        return ResponseEntity.ok(CommonApiResponse.ok(likeResponse));
    }

    // 좋아요 해제
    @DeleteMapping
    public ResponseEntity<CommonApiResponse<Void>> unlike(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long boardId
    ) {
        likeService.unlike(userId, boardId);
        return ResponseEntity.ok(CommonApiResponse.ok());
    }

    // 토글
    @PostMapping("/toggle")
    public ResponseEntity<CommonApiResponse<LikeToggleResponse>> toggle(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long boardId
    ) {
        LikeToggleResponse likeToggleResponse = likeService.toggle(userId, boardId);
        return ResponseEntity.ok(CommonApiResponse.ok(likeToggleResponse));
    }

    // 개수
    @GetMapping("/count")
    public ResponseEntity<CommonApiResponse<Long>> count(@PathVariable Long boardId) {
        long cnt = likeService.count(boardId);
        return ResponseEntity.ok(CommonApiResponse.ok(cnt));
    }

    // 내가 좋아요 중인지
    @GetMapping("/me")
    public ResponseEntity<CommonApiResponse<Boolean>> isLiked(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long boardId
    ) {
        boolean liked = likeService.isLiked(userId, boardId);
        return ResponseEntity.ok(CommonApiResponse.ok(liked));
    }
}
