package com.project.board0905.domain.admin.entity;

import com.project.board0905.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "admin_audit_logs", indexes = {
        @Index(name = "ix_admin_log_actor", columnList = "actor_id"),
        @Index(name = "ix_admin_log_created", columnList = "createdAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AdminAuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "actor_id", nullable = false)
    private User actor; // 수행 관리자

    @Enumerated(EnumType.STRING) @Column(length = 40, nullable = false)
    private AdminActionType action;

    @Enumerated(EnumType.STRING) @Column(length = 40, nullable = false)
    private AdminTargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(columnDefinition = "text")
    private String metadata; // JSON 등 부가 정보

    @CreatedDate
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

}
