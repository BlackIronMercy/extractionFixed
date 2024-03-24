package com.main.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.main.model.JsonPrompt;

@Repository
public interface JsonPromptRepo extends JpaRepository<JsonPrompt, Integer> {

	Optional<JsonPrompt> findByStatusAndDocumentType(String activeStatus, String documentType);

	Optional<JsonPrompt> findByStatusAndDocumentTypeAndCompanyNameAndPromptType(String activeStatus,
			String documentType, String companyName, String promptType);

	
	
}