package com.project.board0905.domain.admin.service;

import com.project.board0905.common.error.BusinessException;
import com.project.board0905.domain.admin.dto.AdminBoardUpdateRequest;
import com.project.board0905.domain.admin.dto.AdminRoleUpdateRequest;
import com.project.board0905.domain.admin.dto.AdminUserStatusUpdateRequest;
import com.project.board0905.domain.admin.entity.AdminActionType;
import com.project.board0905.domain.admin.entity.AdminAuditLog;
import com.project.board0905.domain.admin.entity.AdminTargetType;
import com.project.board0905.domain.admin.repository.AdminAuditLogRepository;
import com.project.board0905.domain.board.dto.BoardResponse;
import com.project.board0905.domain.board.entity.Board;
import com.project.board0905.domain.board.repository.BoardRepository;
import com.project.board0905.domain.category.entity.Category;
import com.project.board0905.domain.category.repository.CategoryRepository;
import com.project.board0905.domain.user.dto.UserResponse;
import com.project.board0905.domain.user.entity.Role;
import com.project.board0905.domain.user.entity.User;
import com.project.board0905.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.project.board0905.domain.admin.error.AdminErrorCode.*;
import static com.project.board0905.domain.board.error.BoardErrorCode.BOARD_NOT_FOUND;
import static com.project.board0905.domain.user.error.UserErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CategoryRepository categoryRepository;
    private final AdminAuditLogRepository auditLogRepository;

    // 권한체크
    private User getAdminOrThrow(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
        if (admin.getRole() != Role.ADMIN) {
            throw new BusinessException(NOT_ADMIN);
        }
        return admin;
    }

    private void log(User admin, AdminActionType action, AdminTargetType targetType, Long targetId, String metadata) {
        auditLogRepository.save(AdminAuditLog.builder()
                .actor(admin)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .metadata(metadata)
                .build());
    }

    // 회원관리
    @Override
    public Page<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::of);
    }

    @Override
    public UserResponse updateUserRole(Long adminId, Long userId, AdminRoleUpdateRequest adminRoleUpdateRequest) {
        User admin = getAdminOrThrow(adminId);
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
        user.changeRole(adminRoleUpdateRequest.getRole());
        log(admin, AdminActionType.USER_ROLE_UPDATE, AdminTargetType.USER, userId, "{\"role\":\""+adminRoleUpdateRequest.getRole()+"\"}");
        return UserResponse.of(user);
    }

    @Override
    public UserResponse updateUserStatus(Long adminId, Long userId, AdminUserStatusUpdateRequest adminUserStatusUpdateRequest) {
        User admin = getAdminOrThrow(adminId);
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
        user.changeStatus(adminUserStatusUpdateRequest.getStatus());
        log(admin, AdminActionType.USER_STATUS_UPDATE, AdminTargetType.USER, userId, "{\"status\":\""+adminUserStatusUpdateRequest.getStatus()+"\"}");
        return UserResponse.of(user);
    }

    // 게시글 관리
    @Override
    public Page<BoardResponse> listBoards(Pageable pageable) {
        return boardRepository.findAll(pageable).map(BoardResponse::of);
    }

    @Override
    public BoardResponse updateBoard(Long adminId, Long boardId, AdminBoardUpdateRequest adminBoardUpdateRequest) {
        User admin = getAdminOrThrow(adminId);
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new BusinessException(BOARD_NOT_FOUND));

        if (adminBoardUpdateRequest.getCategoryId() != null) {
            Category category = categoryRepository.findById(adminBoardUpdateRequest.getCategoryId())
                    .orElseThrow(() -> new BusinessException(TARGET_NOT_FOUND));
            board.change(board.getTitle(), board.getContent(), category, board.isNotice());
        }
        if (adminBoardUpdateRequest.getTitle() != null) {
            board.change(adminBoardUpdateRequest.getTitle(), (adminBoardUpdateRequest.getContent() == null ? board.getContent() : adminBoardUpdateRequest.getContent()),
                    (adminBoardUpdateRequest.getCategoryId() == null ? board.getCategory() : board.getCategory()),
                    (adminBoardUpdateRequest.getNotice() == null ? board.isNotice() : adminBoardUpdateRequest.getNotice()));
        } else if (adminBoardUpdateRequest.getContent() != null || adminBoardUpdateRequest.getNotice() != null) {
            board.change(board.getTitle(),
                    (adminBoardUpdateRequest.getContent() == null ? board.getContent() : adminBoardUpdateRequest.getContent()),
                    board.getCategory(),
                    (adminBoardUpdateRequest.getNotice() == null ? board.isNotice() : adminBoardUpdateRequest.getNotice()));
        }

        if (Boolean.TRUE.equals(adminBoardUpdateRequest.getDeleted())) {
            if (board.isDeleted()) throw new BusinessException(BOARD_ALREADY_DELETED);
            board.softDelete();
        } else if (Boolean.FALSE.equals(adminBoardUpdateRequest.getDeleted())) {
            if (!board.isDeleted()) throw new BusinessException(BOARD_NOT_DELETED);
            // restore
            // Board 엔티티에 restore()가 없으면 아래처럼
            // b.setDeleted(false); // setter 없다면 change 메서드로 상태만 바꾸는 별도 메서드 추가 권장
            // 간단히:
            try {
                var f = Board.class.getDeclaredField("isDeleted");
                f.setAccessible(true);
                f.set(board, false);
            } catch (Exception ignore) { /* 엔티티에 restore() 추가 권장 */ }
        }

        log(admin, AdminActionType.BOARD_UPDATE, AdminTargetType.BOARD, boardId, null);
        return BoardResponse.of(board);
    }

    @Override
    public void softDeleteBoard(Long adminId, Long boardId) {
        User admin = getAdminOrThrow(adminId);
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new BusinessException(BOARD_NOT_FOUND));
        if (board.isDeleted()) throw new BusinessException(BOARD_ALREADY_DELETED);
        board.softDelete();
        log(admin, AdminActionType.BOARD_DELETE_SOFT, AdminTargetType.BOARD, boardId, null);
    }

    @Override
    @Transactional
    public void restoreBoard(Long adminId, Long boardId) {
        User admin = getAdminOrThrow(adminId);
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new BusinessException(BOARD_NOT_FOUND));
        if (!board.isDeleted()) throw new BusinessException(BOARD_NOT_DELETED);
        try {
            var f = Board.class.getDeclaredField("isDeleted");
            f.setAccessible(true);
            f.set(board, false);
        } catch (Exception ignore) { }
        log(admin, AdminActionType.BOARD_RESTORE, AdminTargetType.BOARD, boardId, null);
    }

    @Override
    public BoardResponse toggleNotice(Long adminId, Long boardId, boolean notice) {
        User admin = getAdminOrThrow(adminId);
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new BusinessException(BOARD_NOT_FOUND));
        board.change(board.getTitle(), board.getContent(), board.getCategory(), notice);
        log(admin, AdminActionType.BOARD_NOTICE_TOGGLE, AdminTargetType.BOARD, boardId, "{\"notice\":"+notice+"}");
        return BoardResponse.of(board);
    }

}
