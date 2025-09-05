package com.project.board0905.domain.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BoardCreateRequest {

    private Long authorId;      // 로그인 사용자를 주입받는다면 생략 가능(@AuthenticationPrincipal)
    private Integer categoryId; // nullable

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String content;

    private boolean notice;
}
