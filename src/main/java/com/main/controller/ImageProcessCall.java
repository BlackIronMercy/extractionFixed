package com.main.controller;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.main.common.DataContainer;
import com.main.common.GlobalConstant;
import com.main.model.JsonPrompt;
import com.main.repo.JsonPromptRepo;

import kong.unirest.Unirest;

@Service
public class ImageProcessCall {

	@Autowired
	JsonPromptRepo jsonPromptRepo;

	@Value("${keyOpenAPI}")
	public String keyOpenAPI;
	
	private static final Logger log = LogManager.getLogger(ImageProcessCall.class);

	public String sendUpsertRequestPostCall(String JsonData, String authorizationToken, String ipUrl,
			DataContainer data) {
		String responseOutput = "";
		String jwtToken = authorizationToken;
		log.info("ipUrl: {}", ipUrl);
		log.info("JsonData: {}", JsonData);
		log.info("jwtToken: {}", jwtToken);
		try {

			kong.unirest.HttpResponse<String> response = Unirest.post(ipUrl)
					.header("Authorization", "Bearer " + jwtToken).header("Content-Type", "application/json")
					.body(JsonData).asString();

			log.info("response status: {}", response.getStatus());
			log.info("response body: {}", response.getBody());
			if (response.getStatus() == 200) {
				responseOutput = response.getBody();

			} else {
				responseOutput = response.getBody();

			}
			// output = response.getBody();

		} catch (Exception e) {
			data.setMsg(GlobalConstant.MSG_ERROR);
			log.error("Requested Service is not Up {}", e.toString());
		}
		return responseOutput;

	}

	public String sendUpsertRequestGETCall(HttpServletRequest request, String ipUrl, DataContainer data) {
		String output = "";
		String jwtToken = (String) request.getSession().getAttribute("jwttoken");
		log.info("ipUrl: {}", ipUrl);
		log.info("jwtToken: {}", jwtToken);
		try {

			kong.unirest.HttpResponse<String> response = Unirest.get(ipUrl)
					.header("Authorization", "Bearer " + jwtToken).header("Content-Type", "application/json")
					.asString();

			log.info("response status: {}", response.getStatus());
			log.info("response body: {}", response.getBody());
			if (response.getStatus() == 200) {
				data.setData(response.getBody());
				data.setMsg(GlobalConstant.MSG_SUCCESS);
				output = response.getBody();
			} else {
				data.setMsg(GlobalConstant.MSG_ERROR);
			}

		} catch (Exception e) {
			data.setMsg(GlobalConstant.MSG_ERROR);
			log.error("Requested Service is not Up {}", e.toString());
		}
		return output;

	}

	public String sendOpenAiCall(String JsonData, DataContainer data, String documentType,String companyName,String promptType) {
		String output = "";
		String responseOutput = "";
		String prompt = "";
		// String jwtToken = authorizationToken;
		// log.info("ipUrl: {}", ipUrl);
		log.info("JsonData: {}", JsonData);
		log.info("data:------------------------------------------------------------- {}", data);

		Optional<JsonPrompt> jsonPrompt = jsonPromptRepo.findByStatusAndDocumentTypeAndCompanyNameAndPromptType(GlobalConstant.ACTIVE_STATUS,documentType,
				companyName,promptType);
		if (jsonPrompt.isPresent()) {
			prompt = jsonPrompt.get().getPrompt();
		}

		JSONObject jsonPayload = new JSONObject();
		jsonPayload.put("model", "gpt-3.5-turbo");

		JSONArray messagesArray = new JSONArray();

		JSONObject systemMessage = new JSONObject();
		systemMessage.put("role", "system");
		systemMessage.put("content", prompt);
		messagesArray.put(systemMessage);

		JSONObject userMessage = new JSONObject();
		userMessage.put("role", "user");
		userMessage.put("content", JsonData);
		messagesArray.put(userMessage);

		jsonPayload.put("messages", messagesArray);

		jsonPayload.put("temperature", 0);
		jsonPayload.put("max_tokens", 1800);
		jsonPayload.put("top_p", 1);
		jsonPayload.put("frequency_penalty", 0);
		jsonPayload.put("presence_penalty", 0);

		try {

			kong.unirest.HttpResponse<String> response = Unirest.post("https://api.openai.com/v1/chat/completions")
					.header("Content-Type", "application/json")
					.header("Authorization", "Bearer "+keyOpenAPI)
					.body(jsonPayload.toString()).asString();
			log.info("response status: {}", response.getStatus());
			log.info("response body: {}", response.getBody());
			if (response.getStatus() == 200) {
				responseOutput = response.getBody();

			} else {
				responseOutput = response.getBody();
			}
			// output = response.getBody();

		} catch (Exception e) {
			data.setMsg(GlobalConstant.MSG_ERROR);
			log.error("Requested Service is not Up {}", e.toString());
		}
		return responseOutput;

	}

	public String sendBoePostCall(String JsonData, String ipUrl,
			DataContainer data) {
		String responseOutput = "";
		log.info("ipUrl: {}", ipUrl);
		log.info("JsonData: {}", JsonData);
		try {
			kong.unirest.HttpResponse<String> response = Unirest.post("localhost:8181/extractionController/extractBoe")
					  .header("Content-Type", "application/json")
					  .body("{\r\n    \"fileName\": \"test.pdf\",\r\n    \"fileData\": \""+JsonData+"\"\r\n}")
					  .asString();

			log.info("response status: {}", response.getStatus());
			log.info("response body: {}", response.getBody());
			if (response.getStatus() == 200) {
				responseOutput = response.getBody();

			} else {
				responseOutput = response.getBody();

			}

		} catch (Exception e) {
			data.setMsg(GlobalConstant.MSG_ERROR);
			log.error("Requested Service is not Up {}", e.toString());
		}
		return responseOutput;

	}
	
}
