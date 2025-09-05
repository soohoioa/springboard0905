package com.project.board0905.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentCreateRequest {
    private Long boardId;      // 컨트롤러에서 path var로 받으면 생략 가능
    private Long parentId;     // 루트면 null

    @NotBlank
    private String content;
}
