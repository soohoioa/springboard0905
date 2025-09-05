package com.project.board0905.domain.category.service;

import com.project.board0905.common.error.BusinessException;
import com.project.board0905.domain.category.dto.CategoryCreateRequest;
import com.project.board0905.domain.category.dto.CategoryResponse;
import com.project.board0905.domain.category.dto.CategoryUpdateRequest;
import com.project.board0905.domain.category.entity.Category;
import com.project.board0905.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;

import static com.project.board0905.domain.category.error.CategoryErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse create(CategoryCreateRequest categoryCreateRequest) {
        // 중복 체크
        if (categoryRepository.existsByName(categoryCreateRequest.getName())) {
            throw new BusinessException(CATEGORY_NAME_DUPLICATE);
        }
        String slug = (categoryCreateRequest.getSlug() == null || categoryCreateRequest.getSlug().isBlank())
                ? toSlug(categoryCreateRequest.getName())
                : toSlug(categoryCreateRequest.getSlug());
        if (categoryRepository.existsBySlug(slug)) {
            throw new BusinessException(CATEGORY_SLUG_DUPLICATE);
        }

        Category saved = categoryRepository.save(
                Category.builder()
                        .name(categoryCreateRequest.getName())
                        .slug(slug)
                        .sortOrder(categoryCreateRequest.getSortOrder() == null ? 0 : categoryCreateRequest.getSortOrder())
                        .build()
        );
        return CategoryResponse.of(saved);
    }

    @Override
    public CategoryResponse get(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CATEGORY_NOT_FOUND));
        return CategoryResponse.of(category);
    }

    @Override
    public CategoryResponse getBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException(CATEGORY_NOT_FOUND));
        return CategoryResponse.of(category);
    }

    @Override
    public Page<CategoryResponse> page(Pageable pageable) {
        // 기본 정렬: sortOrder ASC, createdAt DESC (pageable이 정렬 없을 때)
        Sort defaultSort = Sort.by(Sort.Order.asc("sortOrder"), Sort.Order.desc("createdAt"));
        Pageable pageable1 = pageable.getSort().isUnsorted()
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), defaultSort)
                : pageable;
        return categoryRepository.findAll(pageable1).map(CategoryResponse::of);
    }

    @Override
    public List<CategoryResponse> listAllOrderBySortThenCreated() {
        Sort sort = Sort.by(Sort.Order.asc("sortOrder"), Sort.Order.desc("createdAt"));
        return categoryRepository.findAll(sort).stream().map(CategoryResponse::of).toList();
    }

    @Override
    @Transactional
    public CategoryResponse update(Integer id, CategoryUpdateRequest categoryUpdateRequest) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CATEGORY_NOT_FOUND));

        if (categoryUpdateRequest.getName() != null && !categoryUpdateRequest.getName().isBlank()) {
            if (!categoryUpdateRequest.getName().equals(category.getName()) && categoryRepository.existsByName(categoryUpdateRequest.getName())) {
                throw new BusinessException(CATEGORY_NAME_DUPLICATE);
            }
            category.changeName(categoryUpdateRequest.getName());
        }

        if (categoryUpdateRequest.getSlug() != null && !categoryUpdateRequest.getSlug().isBlank()) {
            String newSlug = toSlug(categoryUpdateRequest.getSlug());
            if (!newSlug.equals(category.getSlug()) && categoryRepository.existsBySlug(newSlug)) {
                throw new BusinessException(CATEGORY_SLUG_DUPLICATE);
            }
            category.changeSlug(newSlug);
        }

        if (categoryUpdateRequest.getSortOrder() != null) {
            category.changeSortOrder(categoryUpdateRequest.getSortOrder());
        }

        return CategoryResponse.of(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CATEGORY_NOT_FOUND));
        // 안전 삭제: 현재 FK가 RESTRICT라면, 참조 중이면 삭제 불가.
        // boards.category_id 가 nullable 이지만 FK 옵션이 SET NULL이 아니므로,
        // 서비스 단에서 "참조 없음"을 보장하거나 FK를 SET NULL로 변경하세요.
        // 여기서는 단순히 삭제 시도(참조 있으면 DataIntegrityViolationException 발생).
        categoryRepository.delete(category);
    }

    // 간단 slug 변환기 (한글/공백 → 하이픈, 소문자, 영숫자-하이픈만 남김)
    private String toSlug(String input) {
        String n = Normalizer.normalize(input, Normalizer.Form.NFKD);
        String lowered = n.toLowerCase().trim().replaceAll("\\s+", "-");
        String cleaned = lowered.replaceAll("[^a-z0-9\\-]", "");
        cleaned = cleaned.replaceAll("-{2,}", "-");
        if (cleaned.isBlank()) {
            throw new BusinessException(CATEGORY_SLUG_INVALID);
        }
        return cleaned;
    }
}
