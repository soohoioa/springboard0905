package com.project.board0905.domain.comment.service;

import com.project.board0905.common.error.BusinessException;
import com.project.board0905.domain.board.entity.Board;
import com.project.board0905.domain.board.repository.BoardRepository;
import com.project.board0905.domain.comment.dto.CommentCreateRequest;
import com.project.board0905.domain.comment.dto.CommentResponse;
import com.project.board0905.domain.comment.dto.CommentUpdateRequest;
import com.project.board0905.domain.comment.entity.Comment;
import com.project.board0905.domain.comment.repository.CommentRepository;
import com.project.board0905.domain.user.entity.User;
import com.project.board0905.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.project.board0905.domain.board.error.BoardErrorCode.BOARD_NOT_FOUND;
import static com.project.board0905.domain.comment.error.CommentErrorCode.*;
import static com.project.board0905.domain.user.error.UserErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentResponse create(Long authorId, CommentCreateRequest commentCreateRequest) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        Board board = boardRepository.findById(commentCreateRequest.getBoardId())
                .orElseThrow(() -> new BusinessException(BOARD_NOT_FOUND));

        if (commentCreateRequest.getContent() == null || commentCreateRequest.getContent().isBlank()) {
            throw new BusinessException(CONTENT_REQUIRED);
        }

        Comment saved;
        if (commentCreateRequest.getParentId() == null) {
            // 루트
            saved = commentRepository.save(Comment.newRoot(board, author, commentCreateRequest.getContent()));
        } else {
            // 대댓글
            Comment parent = commentRepository.findById(commentCreateRequest.getParentId())
                    .orElseThrow(() -> new BusinessException(COMMENT_NOT_FOUND));

            if (parent.getDepth() != 0) throw new BusinessException(INVALID_PARENT);
            if (!parent.getBoard().getId().equals(board.getId())) throw new BusinessException(BOARD_MISMATCH);

            saved = commentRepository.save(Comment.newReply(board, author, parent, commentCreateRequest.getContent()));
        }

        // 깊이 검증(안전망)
        if (saved.getDepth() != 0 && saved.getDepth() != 1) {
            throw new BusinessException(INVALID_DEPTH);
        }

        return CommentResponse.of(saved);
    }

    @Override
    public CommentResponse get(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(COMMENT_NOT_FOUND));
        return CommentResponse.of(comment);
    }

    @Override
    public Page<CommentResponse> pageByBoard(Long boardId, Pageable pageable) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(BOARD_NOT_FOUND));
        return commentRepository.findByBoard(board, pageable).map(CommentResponse::of);
    }

    @Override
    public List<CommentResponse> listByBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(BOARD_NOT_FOUND));
        return commentRepository.findByBoardOrderByCreatedAtAsc(board)
                .stream().map(CommentResponse::of).toList();
    }

    @Override
    @Transactional
    public CommentResponse update(Long id, CommentUpdateRequest req, Long authorId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(COMMENT_NOT_FOUND));
        // 권한 체크가 필요하면 여기서 authorId 비교(또는 관리자 권한 별도 처리)
        if (req.getContent() == null || req.getContent().isBlank()) {
            throw new BusinessException(CONTENT_REQUIRED);
        }
        comment.changeContent(req.getContent());
        return CommentResponse.of(comment);
    }

    @Override
    @Transactional
    public void delete(Long id, Long authorId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(COMMENT_NOT_FOUND));
        // 권한 체크 필요 시 authorId 비교
        comment.softDelete();
    }
}
