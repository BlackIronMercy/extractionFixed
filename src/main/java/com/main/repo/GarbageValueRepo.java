package com.main.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.main.model.GarbageValues;

@Repository
public interface GarbageValueRepo extends JpaRepository<GarbageValues, Integer> {

	List<GarbageValues> findByStatusOrderByOrderReplace(String activeStatus);
	
	
}