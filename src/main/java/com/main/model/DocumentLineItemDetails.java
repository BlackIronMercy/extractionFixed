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
@Table(name = "bpaas_ex_document_lineitem_details")
@Data
public class DocumentLineItemDetails implements Serializable {

	private static final long serialVersionUID = 1L;
	protected static final String PK = "id";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "item")
	private String item;
	
	@Column(name = "shipped_qty")
	private String shippedQty;
	
	@Column(name = "file_name")
	private String fileName;

	@Column(name = "document_type")
	private String documentType;

	@Column(name = "unique_number")
	private String uniqueNumber;

	
}
