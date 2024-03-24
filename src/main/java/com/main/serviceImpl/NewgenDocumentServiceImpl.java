package com.main.serviceImpl;

import java.io.File;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.common.GlobalConstant;
import com.main.common.ServiceManager;
import com.main.dto.DocumentDTO;
import com.main.model.DocumentHeaderDetiail;
import com.main.model.NewgenWorkItemDetails;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

@Service
public class NewgenDocumentServiceImpl {

	@Autowired
	ServiceManager serviceManager;

	@Value("${urlSessionCreate}")
	public String urlSessionCreate;

	@Value("${passNewgen}")
	public String passNewgen;

	@Value("${urlWorkItemCreate}")
	public String urlWorkItemCreate;

	@Value("${urlDocUpload}")
	public String urlDocUpload;

	@Value("${volumeId}")
	public String volumeId;

	@Value("${cabinetName}")
	public String cabinetName;

	@Value("${documentName}")
	public String documentName;

	private static final Logger log = LogManager.getLogger(NewgenDocumentServiceImpl.class);

	public String addDocumentData(DocumentDTO documentDTO) {
		String responseString = "";
		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = dateFormat.format(currentDate);
		Optional<DocumentHeaderDetiail> documentHeaderDetiail = serviceManager.documentHeaderRepo
				.findByUniqueNumber(documentDTO.getUniqueNumber());
		DocumentHeaderDetiail documentHeaderDetiailObj = new DocumentHeaderDetiail();
		if (!documentHeaderDetiail.isPresent()) {
			log.error("Data is not present in Document Header {}");

		} else {
			documentHeaderDetiailObj = documentHeaderDetiail.get();

			HttpResponse<String> response = Unirest.post(urlSessionCreate).header("password", passNewgen).asString();

			log.info("response status: of Session Api {}", response.getStatus());
			log.info("response body: of Session Api {}", response.getBody());
			if (response.getStatus() == 200) {
				Document xmlDoc = parseXmlString(response.getBody());
				String sessionId = xmlDoc.getElementsByTagName("SessionId").item(0).getTextContent();
				log.info("SessionId: {}", sessionId);
				if (null != sessionId || !"".equalsIgnoreCase(sessionId)) {

					String newgenAttribute = "";
					if (documentDTO.getStatus().equalsIgnoreCase(GlobalConstant.INVOICE_SUCCESS)) {
						newgenAttribute = "<migo_no>" + documentHeaderDetiailObj.getGrnNumber() + "</migo_no>"
								+ "<mm_document_no>" + documentHeaderDetiailObj.getFiscalYear() + "</mm_document_no>"
								+ "<invoice_amount>" + documentHeaderDetiailObj.getInvoiceAmount() + "</invoice_amount>"
								+ "<company_code>" + documentHeaderDetiailObj.getCompanyCode() + "</company_code>"
//								+ "<fiscal_year>" + documentHeaderDetiailObj.getFiscalYear() + "</fiscal_year>"
								+ "<vendor_invoice_no>" + documentHeaderDetiailObj.getInvoiceNo()
								+ "</vendor_invoice_no>" + "<invoice_date>" + documentHeaderDetiailObj.getInvoiceDate()
								+ "</invoice_date>" + "<user_decision>Send for Approval</user_decision>";
						;
//<invoice_date>ACA</invoice_date>
					} else {
						newgenAttribute = "<migo_no>" + documentHeaderDetiailObj.getGrnNumber() + "</migo_no>"
								+ "<mm_document_no>" + documentHeaderDetiailObj.getFiscalYear() + "</mm_document_no>"
								+ "<invoice_amount>" + documentHeaderDetiailObj.getInvoiceAmount() + "</invoice_amount>"
								+ "<company_code>" + documentHeaderDetiailObj.getCompanyCode() + "</company_code>"
//								+ "<fiscal_year>" + documentHeaderDetiailObj.getFiscalYear() + "</fiscal_year>"
								+ "<vendor_invoice_no>" + documentHeaderDetiailObj.getInvoiceNo()
								+ "</vendor_invoice_no>" + "<invoice_date>" + documentHeaderDetiailObj.getInvoiceDate()
								+ "</invoice_date>" + "<user_decision>Send for Scanning</user_decision>"
								+ "<q_po_invoice_cmt>" + "<activityName>Start Event_1</activityName>"
								+ "<raised_by>ocr_test</raised_by>" + "<comments>Key Feilds not Extracted</comments>"
								+ "<raised_on>" + dateString + "</raised_on>" + "</q_po_invoice_cmt>";
					}
					log.info("newgenAttribute: {}", newgenAttribute);
					String encodedXmlPayload = "";
					try {
						encodedXmlPayload = java.net.URLEncoder.encode(newgenAttribute,
								java.nio.charset.StandardCharsets.UTF_8.toString());
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

					log.info("encodedXmlPayload: {}", encodedXmlPayload);
					HttpResponse<String> responseWorkItem = Unirest.post(urlWorkItemCreate + encodedXmlPayload)
							.header("sessionId", sessionId).asString();

					log.info("responseWorkItem status: {}", responseWorkItem.getStatus());
					log.info("responseWorkItem body: {}", responseWorkItem.getBody());
					if (responseWorkItem.getStatus() == 200) {
						xmlDoc = parseXmlString(responseWorkItem.getBody());
						String processID = xmlDoc.getElementsByTagName("ProcessInstanceId").item(0).getTextContent();
						String folderIndex = xmlDoc.getElementsByTagName("FolderIndex").item(0).getTextContent();

						log.info("processID ----------- {}", processID);

						String newGenReq = "{\r\n" + "  \"NGOAddDocumentBDO\": {\r\n" + "    \"cabinetName\": \""
								+ cabinetName + "\",\r\n" + "    \"folderIndex\": \"" + folderIndex + "\",\r\n"
								+ "    \"documentName\": \"" + documentName + "\",\r\n" + "    \"userDBId\": \""
								+ sessionId + "\",\r\n" + "    \"volumeId\": \"" + volumeId + "\",\r\n"
								+ "    \"accessType\": \"I\",\r\n" + "    \"createdByAppName\": \""
								+ documentDTO.getDocumentExtension() + "\",\r\n" + "    \"enableLog\": \"Y\",\r\n"
								+ "    \"versionFlag\": \"\",\r\n" + "    \"textAlsoFlag\": \"\",\r\n"
								+ "    \"ownerType\": \"U\",\r\n" + "    \"ownerIndex\": \"\",\r\n"
								+ "    \"nameLength\": \"\",\r\n" + "    \"thumbNailFlag\": \"N\",\r\n"
								+ "    \"imageData\": \"\",\r\n" + "    \"encrFlag\": \"N\",\r\n"
								+ "    \"passAlgoType\": \"MD5\",\r\n" + "    \"comment\": \"\",\r\n"
								+ "    \"locale\": \"en_US\",\r\n" + "    }\r\n" + "  }\r\n" + "}";

						HttpResponse<String> responseDocUpload = Unirest.post(urlDocUpload).multiPartContent()
								.field("NGOAddDocumentBDO", newGenReq)
								.field("file", new File(documentDTO.getDocumentPath())).asString();

						log.info("responseDocUpload status: {}", responseDocUpload.getStatus());
						log.info("responseDocUpload body: {}", responseDocUpload.getBody());
						if (responseDocUpload.getStatus() == 200) {
							String message = "";
							String docIndex = "";
							String user = "";
							try {
								ObjectMapper objectMapper = new ObjectMapper();
								JsonNode jsonNode = objectMapper.readTree(responseDocUpload.getBody());
								message = jsonNode.path("NGOAddDocumentResponseBDO").path("message").asText();
								docIndex = jsonNode.path("NGOAddDocumentResponseBDO").path("NGOGetDocListDocDataBDO")
										.path("documentIndex").asText();
								user = jsonNode.path("NGOAddDocumentResponseBDO").path("NGOGetDocListDocDataBDO")
										.path("owner").asText();
								System.out.println("Message: " + message);

							} catch (JsonProcessingException e) {
								e.printStackTrace();
							}
							if ((GlobalConstant.DCOUMNET_ADDED_SUCCESSFULLY).equalsIgnoreCase(message)) {

								NewgenWorkItemDetails documentDetailsObj = new NewgenWorkItemDetails();
								documentDetailsObj.setNewgenRequst(newGenReq);
								documentDetailsObj.setNewgenResponse(responseDocUpload.getBody());
								documentDetailsObj.setUniqueNumber(documentDTO.getUniqueNumber());
								documentDetailsObj.setDocIndex(docIndex);
								documentDetailsObj.setCreatedBy(GlobalConstant.ROLE_ADMIN);
								documentDetailsObj.setCreatedOn(new Date());
								documentDetailsObj.setModifiedBy(GlobalConstant.ROLE_ADMIN);
								documentDetailsObj.setModifiedOn(new Date());
								documentDetailsObj.setFolderDocIndex(folderIndex);
								documentDetailsObj.setProcessInstanceId(processID);
								documentDetailsObj.setNewgenWorkitemResponse(responseWorkItem.getBody());
								documentDetailsObj.setStatus(GlobalConstant.ACTIVE_STATUS);
								documentDetailsObj.setDocumentName(documentDTO.getDocumentName());
								documentDetailsObj.setDocumentExtension(documentDTO.getDocumentExtension());
								documentDetailsObj.setDocumentType(documentDTO.getDocType());
								serviceManager.newgenWorkItemDetailsRepo.save(documentDetailsObj);
								responseString = GlobalConstant.TRUE;
							}
						}

					}

				}
			}
		}
		return responseString;
	}

	private static Document parseXmlString(String xml) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource inputSource = new InputSource(new StringReader(xml));
			return builder.parse(inputSource);
		} catch (Exception e) {
			throw new RuntimeException("Error parsing XML", e);
		}
	}

}
