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
@Table(name = "audit_report")
@Data
public class AuditReport implements Serializable {

	private static final long serialVersionUID = 1L;
	protected static final String PK = "id";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "user_name")
	private String userName;
	
	@Column(name = "role", columnDefinition = "TEXT")
	private String role;
	
	@Column(name = "department", columnDefinition = "TEXT")
	private String department;
	
	@Column(name = "designation", columnDefinition = "TEXT")
	private String designation;
	
	@Column(name = "action_time")
	private String actonTime;
	
	@Column(name = "project_name", columnDefinition = "TEXT")
	private String projectName;
	
	@Column(name = "action_type", columnDefinition = "TEXT")
	private String actionType;
	
	@Column(name = "date_time")
	private Date dateTime;
}
