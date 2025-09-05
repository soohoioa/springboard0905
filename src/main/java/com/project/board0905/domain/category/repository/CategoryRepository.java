package com.project.board0905.domain.category.repository;

import com.project.board0905.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
    Optional<Category> findBySlug(String slug);
}
