package com.main.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.main.model.DocumentHeaderDetiail;

public interface DocumentHeaderRepo extends JpaRepository<DocumentHeaderDetiail, Integer> {

	Optional<DocumentHeaderDetiail> findByUniqueNumber(String uniqueNumber);

}
