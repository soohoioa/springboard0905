package com.project.board0905.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordChangeRequest {
    @NotBlank
    private String currentPassword;

    @NotBlank @Size(min = 8, max = 255)
    private String newPassword;

    @NotBlank
    @Size(min = 8, max = 255)
    private String newPasswordConfirm;
}
