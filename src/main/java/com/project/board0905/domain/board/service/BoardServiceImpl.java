package com.project.board0905.domain.board.service;

import com.project.board0905.common.error.BusinessException;
import com.project.board0905.domain.board.dto.BoardCreateRequest;
import com.project.board0905.domain.board.dto.BoardResponse;
import com.project.board0905.domain.board.dto.BoardUpdateRequest;
import com.project.board0905.domain.board.entity.Board;
import com.project.board0905.domain.board.repository.BoardRepository;
import com.project.board0905.domain.category.entity.Category;
import com.project.board0905.domain.category.repository.CategoryRepository;
import com.project.board0905.domain.user.entity.User;
import com.project.board0905.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.project.board0905.domain.board.error.BoardErrorCode.*;
import static com.project.board0905.domain.category.error.CategoryErrorCode.*;
import static com.project.board0905.domain.user.error.UserErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public BoardResponse create(Long authorId, BoardCreateRequest boardCreateRequest) {
        // authorId는 Controller에서 인증 주체로부터 주입 받는 것을 권장
        Long writerId = (authorId != null) ? authorId : boardCreateRequest.getAuthorId();
        User author = userRepository.findById(writerId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        Category category = null;
        if (boardCreateRequest.getCategoryId() != null) {
            category = categoryRepository.findById(boardCreateRequest.getCategoryId())
                    .orElseThrow(() -> new BusinessException(CATEGORY_NOT_FOUND));
        }

        if (boardCreateRequest.getTitle() == null || boardCreateRequest.getTitle().isBlank()) {
            throw new BusinessException(TITLE_REQUIRED);
        }
        if (boardCreateRequest.getContent() == null || boardCreateRequest.getContent().isBlank()) {
            throw new BusinessException(CONTENT_REQUIRED);
        }

        Board saved = boardRepository.save(
                Board.builder()
                        .author(author)
                        .category(category)
                        .title(boardCreateRequest.getTitle())
                        .content(boardCreateRequest.getContent())
                        .isNotice(boardCreateRequest.isNotice())
                        .build()
        );
        return BoardResponse.of(saved);
    }

    @Override
    public BoardResponse get(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BOARD_NOT_FOUND));
        return BoardResponse.of(board);
    }

    @Override
    public Page<BoardResponse> page(Pageable pageable) {
        return boardRepository.findAll(pageable).map(BoardResponse::of);
    }

    @Override
    @Transactional
    public BoardResponse update(Long id, BoardUpdateRequest boardUpdateRequest) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BOARD_NOT_FOUND));

        Category category = board.getCategory();
        if (boardUpdateRequest.getCategoryId() != null) {
            category = categoryRepository.findById(boardUpdateRequest.getCategoryId())
                    .orElseThrow(() -> new BusinessException(CATEGORY_NOT_FOUND));
        }

        String title = (boardUpdateRequest.getTitle() != null) ? boardUpdateRequest.getTitle() : board.getTitle();
        String content = (boardUpdateRequest.getContent() != null) ? boardUpdateRequest.getContent() : board.getContent();
        boolean isNotice = (boardUpdateRequest.getNotice() != null) ? boardUpdateRequest.getNotice() : board.isNotice();

        if (title == null || title.isBlank()) throw new BusinessException(TITLE_REQUIRED);
        if (content == null || content.isBlank()) throw new BusinessException(CONTENT_REQUIRED);

        board.change(title, content, category, isNotice);
        return BoardResponse.of(board);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BOARD_NOT_FOUND));
        board.softDelete();
    }

    @Override
    @Transactional
    public void increaseView(Long id) {
        int updated = boardRepository.increaseViewCount(id);
        if (updated == 0) throw new BusinessException(BOARD_NOT_FOUND);
    }
}
