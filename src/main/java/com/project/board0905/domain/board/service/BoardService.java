package com.project.board0905.domain.board.service;

import com.project.board0905.domain.board.dto.BoardCreateRequest;
import com.project.board0905.domain.board.dto.BoardResponse;
import com.project.board0905.domain.board.dto.BoardUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardService {
    BoardResponse create(Long authorId, BoardCreateRequest boardCreateRequest); // authorId는 보안 컨텍스트에서 주입 권장
    BoardResponse get(Long id);
    Page<BoardResponse> page(Pageable pageable);
    BoardResponse update(Long id, BoardUpdateRequest boardUpdateRequest);
    void delete(Long id);               // 소프트 삭제
    void increaseView(Long id);         // 조회수 증가(선택)
}
