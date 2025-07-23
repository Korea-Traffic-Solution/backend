package com.kgu.traffic.domain.report.repository;

import com.kgu.traffic.domain.report.entity.Report;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    @Query("SELECT r FROM Report r WHERE r.status = 'APPROVED' AND r.brand = :brand AND DATE(r.approvedAt) = :date")
    List<Report> findApprovedByBrandAndDate(@Param("brand") String brand, @Param("date") LocalDate date);

    @Query("SELECT COUNT(r) FROM Report r")
    long countAll();

    @Query("SELECT COUNT(r) FROM Report r WHERE MONTH(r.reportedAt) = MONTH(CURRENT_DATE) AND YEAR(r.reportedAt) = YEAR(CURRENT_DATE)")
    long countThisMonth();

    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = 'APPROVED'")
    long countApproved();

    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = 'REJECTED'")
    long countRejected();

    @Query("SELECT r FROM Report r WHERE r.status = 'APPROVED' AND r.brand = :brand")
    List<Report> findApprovedByBrand(@Param("brand") String brand);
}