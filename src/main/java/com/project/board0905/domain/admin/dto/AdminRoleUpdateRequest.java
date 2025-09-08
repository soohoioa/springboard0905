package com.project.board0905.domain.admin.dto;

import com.project.board0905.domain.user.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminRoleUpdateRequest {
    @NotNull
    private Role role; // USER or ADMIN
}
