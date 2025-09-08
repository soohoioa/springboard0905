package com.project.board0905.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserSignUpRequest {
    @NotBlank
    @Size(max = 30)
    private String username;

    @NotBlank @Email
    @Size(max = 255)
    private String email;

    @NotBlank @Size(min = 8, max = 255)
    private String password;

    @NotBlank @Size(min = 8, max = 255)
    private String passwordConfirm;

    public UserCreateRequest toCreateRequest() {
        UserCreateRequest userCreateRequest = new UserCreateRequest();
        userCreateRequest.setUsername(this.username);
        userCreateRequest.setEmail(this.email);
        userCreateRequest.setPassword(this.password);
        return userCreateRequest;
    }
}
