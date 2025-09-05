package com.project.board0905.domain.user.dto;

import com.project.board0905.domain.user.entity.Role;
import com.project.board0905.domain.user.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequest {

    @Size(max = 30)
    private String username;

    @Email
    @Size(max = 255)
    private String email;

    @Size(min = 8, max = 255)
    private String password; // 변경 시에만 채움

    private Role role;
    private UserStatus status;
}
