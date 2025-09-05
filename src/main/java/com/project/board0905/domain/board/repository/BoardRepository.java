package com.project.board0905.domain.board.repository;

import com.project.board0905.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Board b set b.viewCount = b.viewCount + 1 where b.id = :boardId")
    int increaseViewCount(Long boardId);
}
