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
@Table(name = "bpaas_ex_json_prompt")
@Data
public class JsonPrompt implements Serializable {

	private static final long serialVersionUID = 1L;
	protected static final String PK = "id";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "prompt",columnDefinition = "Text")
	private String prompt;

	@Column(name = "document_type")
	private String documentType;
	
	@Column(name = "company_name")
	private String companyName;
	
	@Column(name = "prompt_type")
	private String promptType;

	@Column(name = "status")
	private String status;

}
