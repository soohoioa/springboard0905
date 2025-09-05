package com.project.board0905.domain.search.repository;

import com.project.board0905.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardSearchRepository extends JpaRepository<Board, Long>, BoardSearchRepositoryCustom {

}
