package com.main.serviceImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.main.common.DataContainer;
import com.main.common.GlobalConstant;
import com.main.common.ServiceManager;
import com.main.config.JWTUserDetails;
import com.main.controller.ImageProcessCall;
import com.main.dto.DocumentDTO;
import com.main.dto.Dto;
import com.main.dto.ItemDto;
import com.main.model.CompanyCodeCheck;
import com.main.model.DocumentHeaderDetiail;
import com.main.model.DocumentLineItemDetails;
import com.main.model.DocumentType;
import com.main.model.GarbageValues;
import com.main.service.ModelService;

@Service
public class ModelserviceImpl implements ModelService {

	@Value("${ocrInvoiceCall}")
	public String ocrInvoiceCall;

	@Value("${ocrPyPathLinux}")
	public String ocrPyPathLinux;

	@Value("${ocrFileName}")
	public String ocrFileName;

	@Value("${sourcePath}")
	public String pathSource;

	@Value("${dstPath}")
	public String dstPath;

	@Value("${companyName}")
	public String companyName;

	@Value("${destinationFolderCustomerPo}")
	public String destinationFolderCustomerPo;

	@Value("${destinationFolderPackingList}")
	public String destinationFolderPackingList;

	@Value("${destinationFolderTaxInvoice}")
	public String destinationFolderTaxInvoice;

	@Value("${destinationFolderEwayBill}")
	public String destinationFolderEwayBill;

	@Value("${destinationFolderUnprocessed}")
	public String destinationFolderUnprocessed;

	@Value("${destinationFolderProcessed}")
	public String destinationFolderProcessed;

	@Value("${destinationFolderException}")
	public String destinationFolderException;

	private static final Logger logger = LogManager.getLogger(ModelserviceImpl.class);
	@Autowired
	ServiceManager serviceManager;

	@Autowired
	ImageProcessCall imageProcessCall;

	@Autowired
	NewgenDocumentServiceImpl newgenDocumentServiceImpl;

	@Override
	public String getOcrData(Dto dto) {
		DataContainer data = new DataContainer();
		Gson gson = new GsonBuilder().setDateFormat(GlobalConstant.GSON_DATE_FORMAT).create();
		String parseResponse = "";

		List<String> pdfFiles = listPdfFiles(dto.getSourcePath());
		if (!pdfFiles.isEmpty()) {

			for (String pdfFile : pdfFiles) {
				String s = "";
				String sourceText = "";
				Date currDate = new Date();

				String sourcePath = dto.getSourcePath() + pdfFile;
				try {

					System.out.println("----- SourcePath---- /r/n" + pdfFile);
					String quotedSourcePath = "\"" + sourcePath + "\"";

					System.out.println("----- quotedSourcePath---- /r/n" + quotedSourcePath);

					String[] cmd = { "cmd", "/c", "python OcrMain.py " + quotedSourcePath };
					Process p = Runtime.getRuntime().exec(cmd);

					BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
					BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

					while ((s = stdInput.readLine()) != null) {
						sourceText = sourceText + "\n" + s;
					}
					System.out.println("OCR DATA :: " + sourceText);
					System.out.println("Here is the standard error of the command (if any):\n");
					while ((s = stdError.readLine()) != null) {
						System.out.println(s);
					}
					System.out.println("-----------SOURCETEXT____________________------------------" + sourceText);
					dto.setJson(sourceText);
					data.setData(dto);
				} catch (IOException e) {
					System.out.println("exception happened - here's what I know: ");
					e.printStackTrace();
					data.setMsg(e.toString());
				}

				String responseLabel = "";
				String response = "";
				String jsonResponse = "";

				ItemDto itemDto = new ItemDto();

				if (sourceText.toLowerCase().contains("Purchase Order".toLowerCase())
						|| sourceText.toLowerCase().contains("TAX INVOICE".toLowerCase())
						|| sourceText.toLowerCase().contains("Tax Invoice".toLowerCase())
						|| sourceText.toLowerCase().contains("PO No".toLowerCase())
						|| sourceText.toLowerCase().contains("Tax-Invoice".toLowerCase())
						|| sourceText.toLowerCase().contains("TAXINVOICE".toLowerCase())) {
					dto.setDocumentType(GlobalConstant.DOCUMENT_TYPE_PO);
					dto.setDstFolder(destinationFolderProcessed);
					System.out.println("Document Type :: " + dto.getDocumentType());
					response = getOcrDataInvoice(gson.toJson(dto), dto);
					jsonResponse = getContent(response);

				}
				SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss-SSS");

				String outputDate = outputDateFormat.format(currDate);

				String modPdfFile = outputDate + "_" + pdfFile;
				logger.info(" Filename : {}  ", pdfFile);
				logger.info("Mod Filename : {} ", modPdfFile);
				parseResponse = parseJsonHeaderAndLinitem(jsonResponse, dto, response, sourcePath, modPdfFile,
						sourceText, responseLabel);

				String base64String = "";
				try {
					byte[] fileBytes = Files.readAllBytes(Paths.get(dto.getSourcePath() + pdfFile));
					base64String = Base64.getEncoder().encodeToString(fileBytes);
				} catch (IOException e) {
					e.printStackTrace();
				}
				int lastDotIndex = pdfFile.lastIndexOf(".");
				String name = lastDotIndex != -1 ? pdfFile.substring(0, lastDotIndex) : pdfFile;
				String extension = lastDotIndex != -1 && lastDotIndex < pdfFile.length() - 1
						? pdfFile.substring(lastDotIndex + 1)
						: "";

				DocumentDTO documentDTO = new DocumentDTO();
				documentDTO.setUniqueNumber(dto.getUniqueNumber());
				documentDTO.setEncodedBytes(base64String);
				documentDTO.setDocumentName(name);
				documentDTO.setDocumentExtension(extension);
				documentDTO.setDocType(dto.getDocumentType());
				documentDTO.setDocumentPath(dto.getSourcePath() + pdfFile);
				documentDTO.setFileName(pdfFile);
				documentDTO.setDocumentDestPath(dto.getDstPath() + dto.getDstFolder());
				documentDTO.setStatus(dto.getStatus());
				String newgenResponse = newgenDocumentServiceImpl.addDocumentData(documentDTO);

				Path sourceFilePath = Paths.get(dto.getSourcePath(), pdfFile);
				Path destinationFilePath = Paths.get(dto.getDstPath() + dto.getDstFolder(), modPdfFile);

				try {
					Files.move(sourceFilePath, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
					System.out.println("File moved and renamed successfully.");
				} catch (Exception e) {
					System.out.println(e.toString());
				}

			}
		} else {
			System.out.println("Invalid Directory PATH :");
			return GlobalConstant.INVALID_DIRECTORY_PATH;

		}

		return parseResponse;
	}

	public String getOcrDataInvoice(String jsonGson, Dto dto) {
		DataContainer data = new DataContainer();
		Gson gson = new GsonBuilder().setDateFormat(GlobalConstant.GSON_DATE_FORMAT).create();

		Dto dtoObj = new Dto();
		String json = dto.getJson();

		System.out.println("----------Before JSON-----------" + json);

		List<GarbageValues> garbageValuesObj = serviceManager.garbageValuesRepo
				.findByStatusOrderByOrderReplace(GlobalConstant.ACTIVE_STATUS);
		if (!garbageValuesObj.isEmpty()) {
			for (GarbageValues garbageValue : garbageValuesObj) {
				String fromReplace = garbageValue.getFromReplace();
				String toReplace = garbageValue.getToReplace();

				if (garbageValue.getIsRagex().equalsIgnoreCase(GlobalConstant.NO_RAGEX)) {
					json = json.replace(fromReplace, toReplace);
				} else {
					json = json.replaceAll(fromReplace, toReplace);
				}
			}
		}

		System.out.println("json--------------- json---------------------json-------------------------" + json);

		dtoObj.setFileName(dto.getFileName());
		dtoObj.setJson(json);

		String response = imageProcessCall.sendOpenAiCall(dtoObj.getJson(), data, dto.getDocumentType(),
				dto.getCompanyName(), dto.getPromptType());

		return response;
	}

	static String getContent(String jsonData) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(jsonData);

			String content = jsonNode.path("choices").path(0).path("message").path("content").asText();
			/*
			 * if (content.isBlank()) { String type =
			 * jsonNode.path("error").path("type").asText(); if
			 * (GlobalConstant.INVALID_REQUEST_ERROR.equalsIgnoreCase(type)) { return
			 * jsonData; } }
			 */
			System.out.println(content);
			return content;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	String parseJsonHeaderAndLinitem(String jsonResponse, Dto dto, String errorResponse, String sourcePath,
			String filename, String sourceText, String responseLabel) {
		Date dateCurrent = new Date();
		List<String> emptyFields = new ArrayList<>();

		if (jsonResponse == null || jsonResponse.isEmpty()) {

			DocumentType documentType = new DocumentType();
			documentType.setCreatedOn(dateCurrent);
			documentType.setCreatedBy(GlobalConstant.ROLE_ADMIN);
			documentType.setDocumentType(dto.getDocumentType());
			documentType.setFileName(filename);
			documentType.setCompanyName(dto.getCompanyName());
			documentType.setStatus(GlobalConstant.INVOICE_FAILED);
			documentType.setErrorResponse(errorResponse);
			documentType.setSourcePath(sourcePath);
			documentType.setDestPath(sourcePath);
			serviceManager.documentTypeRepo.save(documentType);
			dto.setDocumentType(GlobalConstant.DOCUMENT_TYPE_UNPROCESSED);
			dto.setDstFolder(destinationFolderException);
			dto.setStatus(GlobalConstant.INVOICE_FAILED);
			return GlobalConstant.MSG_ERROR;

		} else {
			UUID uuid = UUID.randomUUID();

			DocumentType documentType = new DocumentType();
			documentType.setCreatedOn(dateCurrent);
			documentType.setCreatedBy(GlobalConstant.ROLE_ADMIN);
			documentType.setDocumentType(dto.getDocumentType());
			documentType.setFileName(filename);
			documentType.setCompanyName(dto.getCompanyName());
			documentType.setUniqueNumber(uuid.toString());
			documentType.setStatus(GlobalConstant.INVOICE_SUCCESS);
			documentType.setSourcePath(sourcePath);
			documentType.setJsonrResponse(dto.getJson());
			documentType.setDestPath(sourcePath);
			documentType.setOpenAiResponse(jsonResponse);
			DocumentType afterSave = serviceManager.documentTypeRepo.save(documentType);
			dto.setStatus(GlobalConstant.INVOICE_SUCCESS);

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			try {

				if (dto.getDocumentType().equalsIgnoreCase(GlobalConstant.PO_DOCUMENT_TYPE)) {
					String grnNumber = "";
					String grnFiscalYear = "";
					DocumentHeaderDetiail documentHeaderDetiailObj = new DocumentHeaderDetiail();

					JsonNode jsonNode = objectMapper.readTree(jsonResponse);

					grnNumber = jsonNode.path("GRN Number").asText();
					String invoiceNumber = jsonNode.path("Invoice Number").asText();
					String invoiceDate = jsonNode.path("Invoice Date").asText();
					String companyCode = jsonNode.path("Company Code").asText();
					String invoiceAmount = jsonNode.path("Invoice Amount").asText();
					String fiscalYear = jsonNode.path("Fiscal Year").asText();
					String hsnSacHeader = jsonNode.path("HSN/SAC").asText();
					String purchaseOrderNumer = jsonNode.path("Purchase Order Number").asText();
					String vendorGstin = jsonNode.path("Vendor GSTIN").asText();
					String companyGstin = jsonNode.path("Company GSTIN").asText();

					if (null != invoiceAmount && !("").equalsIgnoreCase(invoiceAmount) && invoiceAmount.contains(".")) {
						String[] amountparts = invoiceAmount.split("\\.");
						if (amountparts.length > 0) {
							invoiceAmount = amountparts[0];
						}
					}

					if (null != invoiceDate && !("").equalsIgnoreCase(invoiceDate)) {
						String[] parts = invoiceDate.split("-");
						if (parts.length > 0) {
							fiscalYear = parts[0];
						}
					} else {
						fiscalYear = "2023";
					}

					if (fiscalYear.contains("-")) {
						String[] parts = fiscalYear.split("-");

						if (parts.length > 1) {
							fiscalYear = parts[0];
							System.out.println("fiscalYear: conatins - :" + fiscalYear);
						}
					}
					if (null == grnNumber || ("").equalsIgnoreCase(grnNumber)) {
						grnNumber = getGrnNumber(grnNumber, "", dto.getJson());
					}

					List<String> gstinList = new ArrayList<>();
					gstinList.add(vendorGstin);
					gstinList.add(companyGstin);

					List<CompanyCodeCheck> companycodeList = serviceManager.companyCodeCheckRepo
							.findByCompanyGstNumberInAndStatus(gstinList, GlobalConstant.ACTIVE_STATUS);
					if (!companycodeList.isEmpty()) {
						companyCode = companycodeList.get(0).getCompanycode();
					}
					System.out.println("compan Code: " + companyCode);

					checkAndAddToList(emptyFields, "Grn Number ", grnNumber);
					checkAndAddToList(emptyFields, "Company Code ", companyCode);
					checkAndAddToList(emptyFields, "Fiscal Year ", fiscalYear);
					checkAndAddToList(emptyFields, "Invoice Number", invoiceNumber);
					checkAndAddToList(emptyFields, "Invoice Date ", invoiceDate);
					checkAndAddToList(emptyFields, "Invoice Amount ", invoiceAmount);

					if (!emptyFields.isEmpty()) {
						dto.setDstFolder(destinationFolderException);
						// dto.setExceptionRemark(GlobalConstant.KEY_VALUE_NOT_PRESENT);
						dto.setStatus(GlobalConstant.INVOICE_FAILED);
						// String errorMetadataString =
						// emptyFields.stream().collect(Collectors.joining(","));
						dto.setDstFolder(destinationFolderException);
						// afterSave.setExceptionRemark(dto.getExceptionRemark());
						// afterSave.setErrorMetadataString(errorMetadataString);

						afterSave.setStatus(dto.getStatus());
						serviceManager.documentTypeRepo.save(afterSave);
					}

					documentHeaderDetiailObj.setGrnNumber(grnNumber);
					documentHeaderDetiailObj.setInvoiceNo(invoiceNumber);
					documentHeaderDetiailObj.setInvoiceDate(invoiceDate);
					documentHeaderDetiailObj.setCompanyCode(companyCode);
					documentHeaderDetiailObj.setInvoiceAmount(invoiceAmount);
					documentHeaderDetiailObj.setFiscalYear(fiscalYear);
					documentHeaderDetiailObj.setHsnSac(hsnSacHeader);
					documentHeaderDetiailObj.setPurchaseOrderNumber(purchaseOrderNumer);
					documentHeaderDetiailObj.setUniqueNumber(uuid.toString());
					documentHeaderDetiailObj.setFileName(filename);
					documentHeaderDetiailObj.setDocumentType(dto.getDocumentType());
					serviceManager.documentHeaderRepo.save(documentHeaderDetiailObj);
					dto.setUniqueNumber(uuid.toString());

					JsonNode lineItemArray = jsonNode.path("LineItem");
					for (JsonNode lineItemNodeObj : lineItemArray) {
						DocumentLineItemDetails documentLineItemDetailsObj = new DocumentLineItemDetails();

						String itemDesc = lineItemNodeObj.path("Description").asText();
						String orderQuantity = lineItemNodeObj.path("HSN/SAC").asText();
						String purchaseOrderNumber = lineItemNodeObj.path("Chal. No.").asText();
						String Unit = lineItemNodeObj.path("Unit").asText();
						String Rate = lineItemNodeObj.path("Rate").asText();
						String Quantity = lineItemNodeObj.path("quantity").asText();
						String Amount = lineItemNodeObj.path("Amount").asText();
						documentLineItemDetailsObj.setUniqueNumber(uuid.toString());
						documentLineItemDetailsObj.setFileName(filename);
						documentLineItemDetailsObj.setDocumentType(dto.getDocumentType());
						serviceManager.documentLineItemDetailsRepo.save(documentLineItemDetailsObj);

					}
				} else if (dto.getDocumentType().equalsIgnoreCase(GlobalConstant.NON_PO_DOCUMENT_TYPE)) {
					String grnNumber = "";
					String grnFiscalYear = "";
					String customerGstin = "";
					String amount = "";
					DocumentHeaderDetiail documentHeaderDetiailObj = new DocumentHeaderDetiail();

					JsonNode jsonNode = objectMapper.readTree(jsonResponse);

					/*
					 * String grnNumber1 = jsonNode.path("GRN").asText(); String grnNumber2 =
					 * jsonNode.path("GEN").asText();
					 */
//					grnFiscalYear = jsonNode.path("GRN Date").asText();

					String vendorName = jsonNode.path("Vendor Name").asText();
					String vendorEmail = jsonNode.path("Vendor Email").asText();
					String vendorContact = jsonNode.path("Vendor Contact").asText();
					String vendorGstin = jsonNode.path("Vendor GSTN").asText();
					String vendorAddress = jsonNode.path("Vendor Address").asText();
					customerGstin = jsonNode.path("Customer GSTN").asText();
					String customerName = jsonNode.path("Customer Name").asText();
					String customerAddress = jsonNode.path("Customer Address").asText();
					String portOfLoading = jsonNode.path("Port Of Loading").asText();
					String portOfDestination = jsonNode.path("Port Of Destination").asText();
					String containerNo = jsonNode.path("Container No").asText();
					String goCometRefId = jsonNode.path("Go Comet Ref Id").asText();
					String auctionDate = jsonNode.path("Auction Date").asText();
					String vehicleNo = jsonNode.path("Vehicle No").asText();
					String vehicleType = jsonNode.path("Vehicle Type").asText();
					String loadingDate = jsonNode.path("Loading Date").asText();
					String loadingType = jsonNode.path("Loading Type").asText();
					String commercialInvoiceNo = jsonNode.path("Commercial InvoiceNo").asText();
					String blNo = jsonNode.path("BL No").asText();

					String invoiceNumber = jsonNode.path("Invoice No").asText();
					String invoiceDate = jsonNode.path("Invoice Date").asText();
					String purchaseOrderNumer = jsonNode.path("Purchase Order Number").asText();
					String grossamount = jsonNode.path("Gross Amount").asText();
					String netamount = jsonNode.path("Net Amount").asText();
					String sgst = jsonNode.path("SGST").asText();
					String cgst = jsonNode.path("CGST").asText();
					String igst = jsonNode.path("IGST").asText();
					String ewayBillNo = jsonNode.path("EWay Bill No").asText();

					if (grossamount != null && grossamount != "") {
						amount = grossamount;
					} else {
						amount = netamount;
					}

					documentHeaderDetiailObj.setUniqueNumber(uuid.toString());
					documentHeaderDetiailObj.setFileName(filename);
					documentHeaderDetiailObj.setDocumentType(dto.getDocumentType());
					serviceManager.documentHeaderRepo.save(documentHeaderDetiailObj);
					dto.setUniqueNumber(uuid.toString());

					JsonNode lineItemArray = jsonNode.path("Line Item");
					for (JsonNode lineItemNodeObj : lineItemArray) {
						DocumentLineItemDetails documentLineItemDetailsObj = new DocumentLineItemDetails();

						String itemDesc = lineItemNodeObj.path("Description Of Goods").asText();
						String orderQuantity = lineItemNodeObj.path("Order Quantity").asText();
						String purchaseOrderNumber = lineItemNodeObj.path("Purchase Order Number").asText();
						String containerPckage = lineItemNodeObj.path("Container_Package").asText();
						String containerQty = lineItemNodeObj.path("Container_Qty").asText();
						String hsnSac = lineItemNodeObj.path("HSNSAC").asText();
						String vehicleNoLn = lineItemNodeObj.path("Vehicle_No").asText();
						String amountLn = lineItemNodeObj.path("Net Amount").asText();
						documentLineItemDetailsObj.setUniqueNumber(uuid.toString());
						documentLineItemDetailsObj.setFileName(filename);
						documentLineItemDetailsObj.setDocumentType(dto.getDocumentType());
						serviceManager.documentLineItemDetailsRepo.save(documentLineItemDetailsObj);

					}

				}

			} catch (Exception e) {
				System.out.println("error" + e.toString());
				e.printStackTrace();
			}

		}

		return GlobalConstant.MSG_SUCCESS;
	}

	private static List<String> listPdfFiles(String directoryPath) {
		File directory = new File(directoryPath);

		if (!directory.exists() || !directory.isDirectory()) {
			System.out.println("Invalid directory path");
			return Collections.emptyList();
		}

		return Arrays.stream(directory.listFiles()).filter(file -> file.isFile()
				&& (file.getName().toLowerCase().endsWith(".pdf") || file.getName().toLowerCase().endsWith(".png")))
				.map(File::getName).collect(Collectors.toList());
	}

	private static String getLabel(Dto dto) {
		String combined = "";
		if (dto.getDocumentType().equalsIgnoreCase(GlobalConstant.DOCUMENT_TYPE_LABEL)) {
			String json = dto.getJson();
			String modelRegex = "Model: ([^\\n]+)";
			String srNoRegex = "Sr\\. No.: ([^;]+)";
			String sNRegax = "S/N: (\\d+)";

			String model = extractValue(json, modelRegex);
			String srNo = extractValue(json, srNoRegex);
			String srNo1 = extractValue(json, sNRegax);

			if (srNo != null && !srNo.equals("")) {
				System.out.println("Model: " + model.split("/")[0]);
				System.out.println("Sr. No.: " + srNo);

				combined = srNo + "_TS" + model.split("/")[0];
			} else {
				System.out.println("Model: " + model.split("/")[0]);
				System.out.println("S/N: " + srNo1);

				combined = srNo1 + "_TS" + model.split("/")[0];
			}

		}

		return combined;
	}

	private static String extractValue(String input, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);

		if (matcher.find()) {
			return matcher.group(1).trim();
		}

		return "";
	}

	private static String getFiscalYear(String invoiceDate) {
		String fiscalYear = "";
		if (invoiceDate != null || invoiceDate != "") {
			if (invoiceDate.contains("/")) {
				String[] parts = invoiceDate.split("/");
				fiscalYear = parts[parts.length - 1];
			} else {
				String[] parts = invoiceDate.split("-");
				fiscalYear = parts[parts.length - 1];
			}
			if (fiscalYear.length() == 2) {
				fiscalYear = GlobalConstant.CURRENT_CENTURY + fiscalYear;
			}

		}

		return fiscalYear;
	}

	private static String getGrnNumber(String grnNumber1, String grnNumber2, String sourceText) {
		String grnNumber = "";

		if (grnNumber1 != "" && grnNumber1 != null) {
			grnNumber = grnNumber1;
		} else if (grnNumber2 != "" && grnNumber2 != null) {
			grnNumber = grnNumber2;
		}
		if (grnNumber.equals("") || grnNumber == null) {
			String grnRegex = "GRN\\s*:\\s*(\\d+)";
			String genlRegex = "GEN\\s*:\\s*(\\d+)";
			String grnRegex1 = "GRN : (\\d+)";
			String regaxGrn = extractValue(sourceText, grnRegex);
			String regaxGen = extractValue(sourceText, genlRegex);
			String regaxGrn1 = extractValue(sourceText, grnRegex1);
			if (regaxGrn != "" && regaxGrn != null) {
				grnNumber = regaxGrn;
			} else if (regaxGen != "" && regaxGen != null) {
				grnNumber = regaxGen;
			} else if (regaxGen != "" && regaxGen != null) {
				grnNumber = regaxGrn1;
			}

		}

		return grnNumber;
	}

	private static boolean isCustomerGSTN(String checkGSTN, String customerGSTN) {
		return checkGSTN.contains(customerGSTN);
	}

	private static String geteWayBill(Dto dto, ItemDto itemDto) {
		String result = "";
		String json = dto.getJson();
		String regexPattern = "Document Details ([^\\n]+?\\d{4}/\\d{4})";
		String regaxPattern1 = "Document Detate ([^\\n]+?\\d{4}/\\d{4})";

		String regax1 = extractValue(json, regexPattern);
		String regax2 = extractValue(json, regaxPattern1);

		if (regax1 != null && !regax1.isEmpty()) {
			itemDto.setGstInvoiceNo(regax1.split("-")[1]);
			itemDto.setOrderDate(regax1.split("-")[2]);
		} else {
			itemDto.setGstInvoiceNo(regax2.split("-")[1]);
			itemDto.setOrderDate(regax2.split("-")[2]);
		}

		return result;
	}

	private static String getCustomerPo(Dto dto) {
		String radioGraphy = "";
		String json = dto.getJson();

		String poNumberPattern = "PO No\\.\\s*\"\\|\\s*([^\n]+)";
		String poDatePattern = "PO Date\\s*([\\d]{2}-[a-zA-Z]{3}-[\\d]{2})";

		String poNo = extractValue(json, poNumberPattern);
		String poDate = extractValue(json, poDatePattern);

		radioGraphy = poNo + "~" + poDate;

		return radioGraphy;
	}

	private static String getPackingListData(Dto dto) {
		String response = "";
		String json = dto.getJson();

		String customerPoRegax = "Customer PO (.+?)\\s";

		String poNo = extractValue(json, customerPoRegax);

		response = poNo;

		return response;
	}

	public String getBoeDataJson(String jsonGson, Dto dto) {
		DataContainer data = new DataContainer();
		Gson gson = new GsonBuilder().setDateFormat(GlobalConstant.GSON_DATE_FORMAT).create();

		Dto dtoObj = new Dto();
		String json = dto.getJson();

		System.out.println("----------Before JSON-----------" + json);

		List<GarbageValues> garbageValuesObj = serviceManager.garbageValuesRepo
				.findByStatusOrderByOrderReplace(GlobalConstant.ACTIVE_STATUS);
		if (!garbageValuesObj.isEmpty()) {
			for (GarbageValues garbageValue : garbageValuesObj) {
				String fromReplace = garbageValue.getFromReplace();
				String toReplace = garbageValue.getToReplace();

				if (garbageValue.getIsRagex().equalsIgnoreCase(GlobalConstant.NO_RAGEX)) {
					json = json.replace(fromReplace, toReplace);
				} else {
					json = json.replaceAll(fromReplace, toReplace);
				}
			}
		}

		System.out.println("json--------------- json---------------------json-------------------------" + json);

		dtoObj.setFileName(dto.getFileName());
		dtoObj.setJson(json);

		String response = imageProcessCall.sendBoePostCall(dtoObj.getJson(), dto.getDocumentType(), data);

		return response;
	}

	public String getPurchaseOrderRelease(String purchaseOrderNumber) {
		String poRelease = "";
		if (purchaseOrderNumber != null && !purchaseOrderNumber.isEmpty() && purchaseOrderNumber.contains("-")) {
			poRelease = purchaseOrderNumber.split("-")[1];
		}

		return poRelease;
	}

	private void checkAndAddToList(List<String> list, String fieldName, String value) {
		if (value == null || value.isEmpty() || "".equalsIgnoreCase(value.trim())) {
			list.add(fieldName);
		}
	}
}
