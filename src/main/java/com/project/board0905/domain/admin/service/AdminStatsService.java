package com.project.board0905.domain.admin.service;

import com.project.board0905.domain.admin.model.AdminOverviewStats;

import java.time.LocalDate;

public interface AdminStatsService {
    AdminOverviewStats overview(LocalDate from, LocalDate to, int topN);
}
