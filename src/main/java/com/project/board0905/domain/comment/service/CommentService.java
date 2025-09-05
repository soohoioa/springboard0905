package com.project.board0905.domain.comment.service;

import com.project.board0905.domain.comment.dto.CommentCreateRequest;
import com.project.board0905.domain.comment.dto.CommentResponse;
import com.project.board0905.domain.comment.dto.CommentUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {
    CommentResponse create(Long authorId, CommentCreateRequest commentCreateRequest);   // authorId는 인증 주체
    CommentResponse get(Long id);
    Page<CommentResponse> pageByBoard(Long boardId, Pageable pageable);
    List<CommentResponse> listByBoard(Long boardId);                    // 전체(정렬된) 목록
    CommentResponse update(Long id, CommentUpdateRequest commentUpdateRequest, Long authorId);
    void delete(Long id, Long authorId);
}
