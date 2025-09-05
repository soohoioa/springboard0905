package com.project.board0905.domain.board.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BoardUpdateRequest {

    private Integer categoryId;   // nullable
    @Size(max = 200)
    private String title;         // nullable
    private String content;       // nullable
    private Boolean notice;       // nullable
}
