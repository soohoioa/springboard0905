package com.project.board0905.domain.user.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class UserPageResponse {
    private List<UserResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static UserPageResponse of(Page<UserResponse> page) {
        return UserPageResponse.builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
