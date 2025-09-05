package com.project.board0905.domain.category.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryUpdateRequest {

    @Size(max = 50)
    private String name;

    @Size(max = 50)
    private String slug;

    private Integer sortOrder;
}
