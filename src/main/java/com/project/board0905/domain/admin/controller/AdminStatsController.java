package com.project.board0905.domain.admin.controller;

import com.project.board0905.common.web.CommonApiResponse;
import com.project.board0905.domain.admin.dto.AdminOverviewStatsResponse;
import com.project.board0905.domain.admin.model.AdminOverviewStats;
import com.project.board0905.domain.admin.service.AdminStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/stats")
@Tag(name = "Admin-Stats", description = "관리자 통계 API")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/overview")
    @Operation(summary = "대시보드 개요 통계", description = "누적/시계열/랭킹 포함. 기본범위: 최근 7일")
    public ResponseEntity<CommonApiResponse<AdminOverviewStatsResponse>> overview(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "5") int topN
    ) {
        AdminOverviewStats stats = adminStatsService.overview(from, to, topN);
        return ResponseEntity.ok(CommonApiResponse.ok(AdminOverviewStatsResponse.from(stats)));
    }

}
