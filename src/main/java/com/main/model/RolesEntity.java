package com.main.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "bpaas_ex_roles")
@Data
public class RolesEntity implements Serializable{

	private static final long serialVersionUID = 1L;
	 protected static final String PK = "id";
	    
		@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Integer id;
		
		@Column(name = "role_name")
		private String roleName;
		
		@Column(name = "status")
		private String status;

		
		
		
}
