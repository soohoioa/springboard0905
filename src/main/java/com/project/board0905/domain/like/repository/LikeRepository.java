package com.project.board0905.domain.like.repository;

import com.project.board0905.domain.like.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByBoard_IdAndUser_Id(Long boardId, Long userId);
    Optional<Like> findByBoard_IdAndUser_Id(Long boardId, Long userId);
    long countByBoard_Id(Long boardId);
    void deleteByBoard_IdAndUser_Id(Long boardId, Long userId);
}
