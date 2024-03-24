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
@Table(name = "bpaas_ex_garbage_values")
@Data
public class GarbageValues implements Serializable{

	private static final long serialVersionUID = 1L;
	 protected static final String PK = "id";
	    
		@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Integer id;
		
		@Column(name = "from_replace",columnDefinition = "Text")
		private String fromReplace;
		
		@Column(name = "to_replace")
		private String toReplace;
		
		@Column(name = "order_replace")
		private String orderReplace;
		
		@Column(name = "status")
		private String status;

		@Column(name = "is_ragex")
		private String isRagex;


		
		
		
}
