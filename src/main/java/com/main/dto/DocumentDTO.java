package com.main.dto;

import java.util.Date;

import lombok.Data;

@Data
public class DocumentDTO {

	private Long id;
	private String parentFolderName;
	private Long parentFolderIndex;
	private Long docIndex;
	private Long documentTypeId;
	private String documentName;
	private String documentExtension;
	private String departmentName;
	private Long departmentFolderIndex;
	private String encodedBytes;
	private String documentPath;
	private String uniqueNumber;
	private Long docTypeId;
	private String docType;
	private String isDeleted;
	private String createdBy;
	private Date createdOn;
	private String modifiedBy;
	private Date modifiedOn;
	private String status;
	private String poNumber;
	private String flowType;
	private String documentStatus;
	private String fileName;
	private String documentDestPath;
	

}
