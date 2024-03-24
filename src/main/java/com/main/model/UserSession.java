package com.main.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "bpaas_ex_user_session")
@Data
public class UserSession implements Serializable {

	private static final long serialVersionUID = 1L;
	protected static final String PK = "id";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "user_name")
	private String userName;
	
	@Column(name = "jwt_token", columnDefinition = "TEXT")
	private String jwtToken;
	
	@Column(name = "expiry_date")
	private Date expiryDate;
	
	@Column(name = "created_date")
	private Date createdDate;
	
	@Column(name = "modified_date")
	private Date modifiedDate;
}
