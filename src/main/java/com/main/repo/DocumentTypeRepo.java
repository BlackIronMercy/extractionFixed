package com.main.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.main.model.DocumentType;

public interface DocumentTypeRepo extends JpaRepository<DocumentType, Integer> {

}
