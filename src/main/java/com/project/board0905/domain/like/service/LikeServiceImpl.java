package com.project.board0905.domain.like.service;

import com.project.board0905.common.error.BusinessException;
import com.project.board0905.domain.board.entity.Board;
import com.project.board0905.domain.board.repository.BoardRepository;
import com.project.board0905.domain.like.dto.LikeResponse;
import com.project.board0905.domain.like.dto.LikeToggleResponse;
import com.project.board0905.domain.like.entity.Like;
import com.project.board0905.domain.like.repository.LikeRepository;
import com.project.board0905.domain.user.entity.User;
import com.project.board0905.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.project.board0905.domain.board.error.BoardErrorCode.BOARD_NOT_FOUND;
import static com.project.board0905.domain.like.error.LikeErrorCode.ALREADY_LIKED;
import static com.project.board0905.domain.like.error.LikeErrorCode.NOT_LIKED;
import static com.project.board0905.domain.user.error.UserErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public LikeResponse like(Long userId, Long boardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(BOARD_NOT_FOUND));

        if (likeRepository.existsByBoard_IdAndUser_Id(boardId, userId)) {
            throw new BusinessException(ALREADY_LIKED);
        }

        try {
            Like saved = likeRepository.save(
                    Like.builder().board(board).user(user).build()
            );
            return LikeResponse.of(saved);
        } catch (DataIntegrityViolationException e) { // 동시성 중복 방어
            throw new BusinessException(ALREADY_LIKED);
        }
    }

    @Override
    @Transactional
    public void unlike(Long userId, Long boardId) {
        if (!likeRepository.existsByBoard_IdAndUser_Id(boardId, userId)) {
            throw new BusinessException(NOT_LIKED);
        }
        likeRepository.deleteByBoard_IdAndUser_Id(boardId, userId);
    }

    @Override
    @Transactional
    public LikeToggleResponse toggle(Long userId, Long boardId) {
        boolean liked;
        if (likeRepository.existsByBoard_IdAndUser_Id(boardId, userId)) {
            likeRepository.deleteByBoard_IdAndUser_Id(boardId, userId);
            liked = false;
        } else {
            like(userId, boardId); // 위에서 동시성/검증 처리
            liked = true;
        }
        long cnt = likeRepository.countByBoard_Id(boardId);
        return new LikeToggleResponse(liked, cnt);
    }

    @Override
    public long count(Long boardId) {
        boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(BOARD_NOT_FOUND));
        return likeRepository.countByBoard_Id(boardId);
    }

    @Override
    public boolean isLiked(Long userId, Long boardId) {
        return likeRepository.existsByBoard_IdAndUser_Id(boardId, userId);
    }
}
