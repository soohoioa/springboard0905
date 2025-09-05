package com.project.board0905.domain.category.entity;

import com.project.board0905.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "categories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_category_name", columnNames = "name"),
                @UniqueConstraint(name = "uq_category_slug", columnNames = "slug")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // INT UNSIGNED

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 50, nullable = false)
    private String slug;

    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    // 비즈니스 메서드
    public void changeName(String name) { this.name = name; }
    public void changeSlug(String slug) { this.slug = slug; }
    public void changeSortOrder(Integer sortOrder) { this.sortOrder = (sortOrder == null ? 0 : sortOrder); }
}
