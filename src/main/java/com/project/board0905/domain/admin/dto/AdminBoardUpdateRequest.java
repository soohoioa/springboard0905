package com.project.board0905.domain.admin.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminBoardUpdateRequest {
    private Integer categoryId;        // null 허용

    @Size(max = 200)
    private String title;              // null 허용
    private String content;            // null 허용
    private Boolean notice;            // null 허용
    private Boolean deleted;           // null 허용 (소프트 삭제/복구)
}
