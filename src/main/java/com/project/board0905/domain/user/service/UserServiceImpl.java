package com.project.board0905.domain.user.service;

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
            throw new IllegalArgumentException("이미 사용 중인 사용자명입니다.");
        }
        if (userRepository.existsByEmail(userCreateRequest.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
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
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
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
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (userUpdateRequest.getUsername() != null && !userUpdateRequest.getUsername().isBlank()) {
            // 본인 이외 중복 체크
            if (userRepository.existsByUsername(userUpdateRequest.getUsername()) && !userUpdateRequest.getUsername().equals(user.getUsername())) {
                throw new IllegalArgumentException("이미 사용 중인 사용자명입니다.");
            }
            user.changeProfile(userUpdateRequest.getUsername(), user.getEmail());
        }

        if (userUpdateRequest.getEmail() != null && !userUpdateRequest.getEmail().isBlank()) {
            if (userRepository.existsByEmail(userUpdateRequest.getEmail()) && !userUpdateRequest.getEmail().equals(user.getEmail())) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
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
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        user.softDelete();
    }
}
