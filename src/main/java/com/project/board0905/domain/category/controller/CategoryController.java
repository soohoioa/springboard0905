package com.project.board0905.domain.category.controller;

import com.project.board0905.common.web.CommonApiResponse;
import com.project.board0905.domain.category.dto.CategoryCreateRequest;
import com.project.board0905.domain.category.dto.CategoryResponse;
import com.project.board0905.domain.category.dto.CategoryUpdateRequest;
import com.project.board0905.domain.category.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
@Tag(name = "Category", description = "카테고리 API")
public class CategoryController {

    private final CategoryService categoryService;

    // 생성
    @PostMapping
    public ResponseEntity<CommonApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CategoryCreateRequest categoryCreateRequest
    ) {
        CategoryResponse response = categoryService.create(categoryCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonApiResponse.ok(response));
    }

    // 단건 조회 (id)
    @GetMapping("/{id}")
    public ResponseEntity<CommonApiResponse<CategoryResponse>> get(@PathVariable Integer id) {
        CategoryResponse response = categoryService.get(id);
        return ResponseEntity.ok(CommonApiResponse.ok(response));
    }

    // 단건 조회 (slug)
    @GetMapping("/slug/{slug}")
    public ResponseEntity<CommonApiResponse<CategoryResponse>> getBySlug(@PathVariable String slug) {
        CategoryResponse response = categoryService.getBySlug(slug);
        return ResponseEntity.ok(CommonApiResponse.ok(response));
    }

    // 페이지 조회
    @GetMapping
    public ResponseEntity<CommonApiResponse<Page<CategoryResponse>>> page(Pageable pageable) {
        Page<CategoryResponse> page = categoryService.page(pageable);
        return ResponseEntity.ok(CommonApiResponse.ok(page));
    }

    // 정렬 리스트 (sortOrder ASC, createdAt DESC)
    @GetMapping("/all")
    public ResponseEntity<CommonApiResponse<List<CategoryResponse>>> listAll() {
        List<CategoryResponse> list = categoryService.listAllOrderBySortThenCreated();
        return ResponseEntity.ok(CommonApiResponse.ok(list));
    }

    // 수정
    @PatchMapping("/{id}")
    public ResponseEntity<CommonApiResponse<CategoryResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryUpdateRequest categoryUpdateRequest
    ) {
        CategoryResponse response = categoryService.update(id, categoryUpdateRequest);
        return ResponseEntity.ok(CommonApiResponse.ok(response));
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonApiResponse<Void>> delete(@PathVariable Integer id) {
        categoryService.delete(id);
        return ResponseEntity.ok(CommonApiResponse.ok(null));
    }
}
