package com.project.board0905.domain.user.service;

import com.project.board0905.domain.user.dto.UserCreateRequest;
import com.project.board0905.domain.user.dto.UserResponse;
import com.project.board0905.domain.user.dto.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse create(UserCreateRequest req);
    UserResponse get(Long id);
    Page<UserResponse> list(Pageable pageable);
    UserResponse update(Long id, UserUpdateRequest req);
    void softDelete(Long id); // status = DELETED
}
