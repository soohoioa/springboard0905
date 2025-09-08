package com.project.board0905.domain.user.service;

import com.project.board0905.domain.user.dto.PasswordChangeRequest;
import com.project.board0905.domain.user.dto.UserCreateRequest;
import com.project.board0905.domain.user.dto.UserResponse;
import com.project.board0905.domain.user.dto.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse create(UserCreateRequest userCreateRequest);
    UserResponse get(Long id);
    Page<UserResponse> list(Pageable pageable);
    UserResponse update(Long id, UserUpdateRequest userUpdateRequest);
    void softDelete(Long id); // status = DELETED

    UserResponse myPage(Long userId);
    UserResponse updateMe(Long userId, UserUpdateRequest userUpdateRequest);
    void changePassword(Long userId, PasswordChangeRequest passwordChangeRequest);
}
