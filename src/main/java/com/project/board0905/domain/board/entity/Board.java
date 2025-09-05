package com.project.board0905.domain.board.entity;

import com.project.board0905.common.BaseTimeEntity;
import com.project.board0905.domain.category.entity.Category;
import com.project.board0905.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(
        name = "boards",
        indexes = {
                @Index(name = "ix_board_author", columnList = "author_id"),
                @Index(name = "ix_board_category", columnList = "category_id"),
                @Index(name = "ix_board_created", columnList = "createdAt")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Board extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY) @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = LAZY) @JoinColumn(name = "category_id")
    private Category category; // nullable

    @Column(length = 200, nullable = false)
    private String title;

    @Lob @Column(nullable = false)
    private String content;

    @Column(columnDefinition = "int unsigned default 0")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(columnDefinition = "tinyint(1) default 0", nullable = false)
    @Builder.Default
    private boolean isNotice = false;

    @Column(columnDefinition = "tinyint(1) default 0", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    public void change(String title, String content, Category category, boolean isNotice) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.isNotice = isNotice;
    }

    public void softDelete() { this.isDeleted = true; }

    public void increaseView() { this.viewCount = (this.viewCount == null ? 1 : this.viewCount + 1); }
}