package com.main.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.main.model.UserSession;

@Repository
public interface UserSessionRepo extends JpaRepository<UserSession, Integer> {

	Optional<UserSession> findByUserName(String username);


	Long countByUserNameAndJwtToken(String username, String token);
	

	
}