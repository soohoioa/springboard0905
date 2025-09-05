package com.project.board0905.domain.comment.entity;

import com.project.board0905.common.BaseTimeEntity;
import com.project.board0905.domain.board.entity.Board;
import com.project.board0905.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(
        name = "comments",
        indexes = {
                @Index(name = "ix_c_board", columnList = "board_id"),
                @Index(name = "ix_c_parent", columnList = "parent_id"),
                @Index(name = "ix_c_created", columnList = "createdAt")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Comment extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY) @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = LAZY) @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = LAZY) @JoinColumn(name = "parent_id")
    private Comment parent;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @Column(nullable = false)
    private int depth; // 0 = 루트, 1 = 대댓글

    @Column(columnDefinition = "tinyint(1) default 0", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    /* 팩토리 */
    public static Comment newRoot(Board board, User author, String content) {
        return Comment.builder()
                .board(board)
                .author(author)
                .content(content)
                .depth(0)
                .isDeleted(false)
                .build();
    }

    public static Comment newReply(Board board, User author, Comment parent, String content) {
        return Comment.builder()
                .board(board)
                .author(author)
                .parent(parent)
                .content(content)
                .depth(1)
                .isDeleted(false)
                .build();
    }

    /* 도메인 동작 */
    public void changeContent(String content) { this.content = content; }

    public void softDelete() {
        this.isDeleted = true;
        this.content = "(삭제된 댓글)";
    }
}
