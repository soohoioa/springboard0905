package com.project.board0905.domain.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryCreateRequest {

    @NotBlank
    @Size(max = 50)
    private String name;

    // 미입력 시 서비스에서 name 기반 slug 생성 가능
    @Size(max = 50)
    private String slug;

    private Integer sortOrder; // null 허용(기본 0)
}
