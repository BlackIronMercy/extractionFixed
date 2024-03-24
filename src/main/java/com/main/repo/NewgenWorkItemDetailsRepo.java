package com.main.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.main.model.AuditReport;
import com.main.model.NewgenWorkItemDetails;

public interface NewgenWorkItemDetailsRepo extends JpaRepository<NewgenWorkItemDetails, Integer> {

}
