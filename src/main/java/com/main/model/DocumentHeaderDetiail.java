package com.main.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Entity
@Table(name = "bpaas_ex_document_header_details")
@Data
public class DocumentHeaderDetiail implements Serializable {

	private static final long serialVersionUID = 1L;
	protected static final String PK = "id";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "document_type")
	private String documentType;

	@Column(name = "file_name")
	private String fileName;

	@Column(name = "unique_number")
	private String uniqueNumber;

	@Column(name = "invoice_no")
	private String invoiceNo;

	@Column(name = "invoice_date")
	private String invoiceDate;

	@Column(name = "net_value")
	private String netValue;

	@Column(name = "grn_number")
	private String grnNumber;

	@Column(name = "company_code")
	private String companyCode;

	@Column(name = "invoice_amount")
	private String invoiceAmount;

	@Column(name = "fiscal_year")
	private String fiscalYear;

	@Column(name = "hsnSac")
	private String hsnSac;
	
	@Column(name = "purchase_order_number")
	private String purchaseOrderNumber;

}
