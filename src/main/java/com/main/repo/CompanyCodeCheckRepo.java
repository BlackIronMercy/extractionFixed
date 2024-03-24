package com.main.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.main.model.CompanyCodeCheck;

public interface CompanyCodeCheckRepo extends JpaRepository<CompanyCodeCheck, Integer> {

	List<CompanyCodeCheck> findByCompanyGstNumberInAndStatus(List<String> gstinList, String activeStatus);

}
