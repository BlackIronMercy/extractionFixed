package com.main.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.main.model.CompanyCodeCheck;
import com.main.repo.CompanyCodeCheckRepo;
import com.main.repo.DocumentHeaderRepo;
import com.main.repo.DocumentLineItemDetailsRepo;
import com.main.repo.DocumentTypeRepo;
import com.main.repo.GarbageValueRepo;
import com.main.repo.NewgenWorkItemDetailsRepo;
import com.main.repo.UserRepo;

@Service
public class ServiceManager {

	@Autowired
	public UserRepo userRepo;

	@Autowired
	public GarbageValueRepo garbageValuesRepo;

	@Autowired
	public DocumentTypeRepo documentTypeRepo;

	@Autowired
	public DocumentHeaderRepo documentHeaderRepo;

	@Autowired
	public DocumentLineItemDetailsRepo documentLineItemDetailsRepo;

	@Autowired
	public CompanyCodeCheckRepo companyCodeCheckRepo;

	@Autowired
	public NewgenWorkItemDetailsRepo newgenWorkItemDetailsRepo;
}
