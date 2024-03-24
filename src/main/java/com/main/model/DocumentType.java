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
@Table(name = "bpaas_ex_document_type")
@Data
public class DocumentType implements Serializable {

	private static final long serialVersionUID = 1L;
	protected static final String PK = "id";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "company_name")
	private String companyName;

	@Column(name = "document_type")
	private String documentType;

	@Column(name = "document_sub_type")
	private String documentSubType;

	@Column(name = "openAi_response", columnDefinition = "Text")
	private String openAiResponse;

	@Column(name = "file_name")
	private String fileName;

	@Column(name = "status")
	private String status;

	@Column(name = "error_response", columnDefinition = "Text")
	private String errorResponse;

	@Column(name = "json_response", columnDefinition = "Text")
	private String jsonrResponse;

	@Column(name = "source_path", columnDefinition = "Text")
	private String sourcePath;

	@Column(name = "dest_path", columnDefinition = "Text")
	private String destPath;

	@Column(name = "unique_number")
	private String uniqueNumber;

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "created_on")
	private Date createdOn;

}
