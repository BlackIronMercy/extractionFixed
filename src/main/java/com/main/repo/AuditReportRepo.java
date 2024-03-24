package com.main.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.main.model.AuditReport;

public interface AuditReportRepo extends JpaRepository<AuditReport, Integer> {

}
