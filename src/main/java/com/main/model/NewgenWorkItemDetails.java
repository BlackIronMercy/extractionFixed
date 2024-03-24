package com.main.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "bpaas_ex_newgen_workitem_detail")
@Data
public class NewgenWorkItemDetails implements Serializable {

	private static final long serialVersionUID = 1L;
	protected static final String PK = "id";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "doc_index")
	private String docIndex;

	@Column(name = "unique_number")
	private String uniqueNumber;

	@Column(name = "document_name")
	private String documentName;

	@Column(name = "folder_doc_index")
	private String folderDocIndex;

	@Column(name = "document_extension")
	private String documentExtension;

	@Column(name = "document_path")
	private String documentPath;

	@Column(name = "document_type")
	private String documentType;

	@Column(name = "flow_type")
	private String flowType;

	@Column(name = "document_type_id")
	private Long documentTypeId;

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "created_on")
	private Date createdOn;

	@Column(name = "modified_by")
	private String modifiedBy;

	@Column(name = "modified_on")
	private Date modifiedOn;

	@Column(name = "status")
	private String status;

	@Column(name = "process_instance_id")
	private String processInstanceId;

	@Column(name = "newgen_requst", columnDefinition = "TEXT")
	private String newgenRequst;

	@Column(name = "newgen_response", columnDefinition = "TEXT")
	private String newgenResponse;

	@Column(name = "newgen_workItrem_response", columnDefinition = "TEXT")
	private String newgenWorkitemResponse;

}