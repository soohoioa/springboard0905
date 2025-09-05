package com.project.board0905.domain.like.entity;

import com.project.board0905.domain.board.entity.Board;
import com.project.board0905.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "likes",
        uniqueConstraints = @UniqueConstraint(name = "uq_like", columnNames = {"board_id", "user_id"}),
        indexes = @Index(name = "ix_like_user", columnList = "user_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Like {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY) @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
