package com.kgu.traffic.domain.report.repository;

import com.kgu.traffic.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}