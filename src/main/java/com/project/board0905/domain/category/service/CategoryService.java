package com.project.board0905.domain.category.service;

import com.project.board0905.domain.category.dto.CategoryCreateRequest;
import com.project.board0905.domain.category.dto.CategoryResponse;
import com.project.board0905.domain.category.dto.CategoryUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    CategoryResponse create(CategoryCreateRequest categoryCreateRequest);
    CategoryResponse get(Integer id);
    CategoryResponse getBySlug(String slug);
    Page<CategoryResponse> page(Pageable pageable); // 페이지네이션
    List<CategoryResponse> listAllOrderBySortThenCreated(); // 정렬 리스트
    CategoryResponse update(Integer id, CategoryUpdateRequest categoryUpdateRequest);
    void delete(Integer id); // 사용 중인 경우 예외
}
