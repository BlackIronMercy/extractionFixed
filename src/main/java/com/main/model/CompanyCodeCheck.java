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
@Table(name = "bpaas_ex_company_code_master")
@Data
public class CompanyCodeCheck implements Serializable {

	private static final long serialVersionUID = 1L;
	protected static final String PK = "id";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "company_name")
	private String companyName;

	@Column(name = "company_full_name", columnDefinition = "Text")
	private String companyFullName;

	@Column(name = "company_gst_number")
	private String companyGstNumber;

	@Column(name = "company_pan_number")
	private String companyPanNumber;

	@Column(name = "company_code")
	private String companycode;

	@Column(name = "status")
	private String status;

}
