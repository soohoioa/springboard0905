package com.project.board0905.domain.user.service;

import com.project.board0905.common.error.BusinessException;
import com.project.board0905.domain.user.dto.PasswordChangeRequest;
import com.project.board0905.domain.user.dto.UserCreateRequest;
import com.project.board0905.domain.user.dto.UserResponse;
import com.project.board0905.domain.user.dto.UserUpdateRequest;
import com.project.board0905.domain.user.entity.Role;
import com.project.board0905.domain.user.entity.User;
import com.project.board0905.domain.user.entity.UserStatus;
import com.project.board0905.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.project.board0905.domain.user.error.UserErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Security 미사용이면 제거하고 패스워드 평문 저장(권장X)

    @Override
    @Transactional
    public UserResponse create(UserCreateRequest userCreateRequest) {
        if (userRepository.existsByUsername(userCreateRequest.getUsername())) {
            throw new BusinessException(USERNAME_DUPLICATE);
        }
        if (userRepository.existsByEmail(userCreateRequest.getEmail())) {
            throw new BusinessException(EMAIL_DUPLICATE);
        }

        User user = User.builder()
                .username(userCreateRequest.getUsername())
                .email(userCreateRequest.getEmail())
                .password(passwordEncoder.encode(userCreateRequest.getPassword()))
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();

        return UserResponse.of(userRepository.save(user));
    }

    @Override
    public UserResponse get(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
        return UserResponse.of(user);
    }

    @Override
    public Page<UserResponse> list(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::of);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        if (userUpdateRequest.getUsername() != null && !userUpdateRequest.getUsername().isBlank()) {
            // 본인 이외 중복 체크
            if (userRepository.existsByUsername(userUpdateRequest.getUsername()) && !userUpdateRequest.getUsername().equals(user.getUsername())) {
                throw new BusinessException(USERNAME_DUPLICATE);
            }
            user.changeProfile(userUpdateRequest.getUsername(), user.getEmail());
        }

        if (userUpdateRequest.getEmail() != null && !userUpdateRequest.getEmail().isBlank()) {
            if (userRepository.existsByEmail(userUpdateRequest.getEmail()) && !userUpdateRequest.getEmail().equals(user.getEmail())) {
                throw new BusinessException(EMAIL_DUPLICATE);
            }
            user.changeProfile(user.getUsername(), userUpdateRequest.getEmail());
        }

        if (userUpdateRequest.getPassword() != null && !userUpdateRequest.getPassword().isBlank()) {
            user.changePassword(passwordEncoder.encode(userUpdateRequest.getPassword()));
        }

        if (userUpdateRequest.getRole() != null) {
            user.changeRole(userUpdateRequest.getRole());
        }

        if (userUpdateRequest.getStatus() != null) {
            user.changeStatus(userUpdateRequest.getStatus());
        }

        return UserResponse.of(user);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
        user.softDelete();
    }

    @Override
    public UserResponse myPage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
        return UserResponse.of(user);
    }

    @Override
    @Transactional
    public UserResponse updateMe(Long userId, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        if (userUpdateRequest.getUsername() != null && !userUpdateRequest.getUsername().isBlank()) {
            if (!userUpdateRequest.getUsername().equals(user.getUsername())
                    && userRepository.existsByUsername(userUpdateRequest.getUsername())) {
                throw new BusinessException(USERNAME_DUPLICATE);
            }
            user.changeProfile(userUpdateRequest.getUsername(), user.getEmail());
        }

        if (userUpdateRequest.getEmail() != null && !userUpdateRequest.getEmail().isBlank()) {
            if (!userUpdateRequest.getEmail().equals(user.getEmail())
                    && userRepository.existsByEmail(userUpdateRequest.getEmail())) {
                throw new BusinessException(EMAIL_DUPLICATE);
            }
            user.changeProfile(user.getUsername(), userUpdateRequest.getEmail());
        }

        return UserResponse.of(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest passwordChangeRequest) {
        if (!passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getNewPasswordConfirm())) {
            throw new BusinessException(PASSWORD_CONFIRM_MISMATCH);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        if (!passwordEncoder.matches(passwordChangeRequest.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(PASSWORD_INCORRECT);
        }
        user.changePassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));

    }
}
