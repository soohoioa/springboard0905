package com.project.board0905.domain.comment.repository;

import com.project.board0905.domain.board.entity.Board;
import com.project.board0905.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 보드 기준 전체(루트+대댓글), 생성일 오름차순
    List<Comment> findByBoardOrderByCreatedAtAsc(Board board);

    Page<Comment> findByBoard(Board board, Pageable pageable);

    // 특정 부모의 대댓글
    List<Comment> findByParentOrderByCreatedAtAsc(Comment parent);
}
