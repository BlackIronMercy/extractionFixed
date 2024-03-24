package com.main.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import com.main.common.GlobalConstant;
import com.main.config.JwtTokenUtil;
import com.main.dto.Dto;
import com.main.service.ModelService;

@RestController
public class ExtractionController {
	private static final Logger logger = LogManager.getLogger(ExtractionController.class);

	@Autowired
	private ModelService modelService;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Value("${ocrInvoiceCall}")
	public String ocrInvoiceCall;

	@Value("${promptType}")
	public String promptType;

	@Value("${sourcePath}")
	public String pathSource;

	@Value("${dstPath}")
	public String dstPath;

	@Value("${companyName}")
	public String companyName;

	@Value("${destinationFolderUnprocessed}")
	public String destinationFolderUnprocessed;

	/*
	 * @CrossOrigin(origins = "${crossOrigin}")
	 * 
	 * @PostMapping("/getOcrData") public String getOcrData(HttpServletRequest
	 * request,
	 * 
	 * @RequestHeader(value = "Authorization", required = true) String
	 * authorizationToken,
	 * 
	 * @RequestBody Dto dto) {
	 * 
	 * DataContainer data = new DataContainer(); Gson gson = new
	 * GsonBuilder().setDateFormat(GlobalConstant.GSON_DATE_FORMAT).create(); String
	 * response =""; try { ObjectMapper mapper = new ObjectMapper();
	 * authorizationToken = jwtTokenUtil.getPureJWTToken(authorizationToken);
	 * logger.info("pure JWT Token in getOcrData {}", authorizationToken);
	 * 
	 * Claims allClaimsFromToken =
	 * jwtTokenUtil.getAllClaimsFromToken(authorizationToken); JWTUserDetails
	 * userDetails =
	 * mapper.convertValue(allClaimsFromToken.get(GlobalConstant.JWTTOKEN_CLAIM),
	 * JWTUserDetails.class);
	 * 
	 * logger.info("userDetails in getOcrData {}", userDetails); response =
	 * modelService.getOcrData( dto); } catch (Exception e) { e.printStackTrace();
	 * data.setMsg(GlobalConstant.MSG_ERROR);
	 * logger.error("error at getOcrData API.. {}", e.toString()); if (e instanceof
	 * AuthorizationException) { throw new AuthorizationException(e.getMessage()); }
	 * throw new ServiceException(e.getMessage()); } return response; }
	 */

	/*
	 * @CrossOrigin(origins = "${crossOrigin}")
	 * 
	 * @PostMapping("/getOcrDataInvoice") public String
	 * getOcrDataInvoice(HttpServletRequest request,
	 * 
	 * @RequestHeader(value = "Authorization", required = true) String
	 * authorizationToken, @RequestBody Dto dto) {
	 * 
	 * DataContainer data = new DataContainer(); Gson gson = new
	 * GsonBuilder().setDateFormat(GlobalConstant.GSON_DATE_FORMAT).create(); String
	 * response = ""; try { ObjectMapper mapper = new ObjectMapper();
	 * authorizationToken = jwtTokenUtil.getPureJWTToken(authorizationToken);
	 * logger.info("pure JWT Token in getOcrDataInvoice {}", authorizationToken);
	 * 
	 * Claims allClaimsFromToken =
	 * jwtTokenUtil.getAllClaimsFromToken(authorizationToken); JWTUserDetails
	 * userDetails =
	 * mapper.convertValue(allClaimsFromToken.get(GlobalConstant.JWTTOKEN_CLAIM),
	 * JWTUserDetails.class);
	 * 
	 * logger.info("userDetails in getOcrDataInvoice {}", userDetails); response =
	 * modelService.getOcrDataInvoice(userDetails, dto); } catch (Exception e) {
	 * e.printStackTrace(); data.setMsg(GlobalConstant.MSG_ERROR);
	 * logger.error("error at getOcrDataInvoice API.. {}", e.toString()); if (e
	 * instanceof AuthorizationException) { throw new
	 * AuthorizationException(e.getMessage()); } throw new
	 * ServiceException(e.getMessage()); } return response; }
	 */
//Scheduler

	@Scheduled(fixedDelayString = "${mqtt.received.message.time}")
	public void kronheScheduler() {

		Dto dto = new Dto();
		List<String> subfolders = getSubfolders(pathSource,destinationFolderUnprocessed);

		if (!subfolders.isEmpty()) {
			for (String subfolder : subfolders) {
				logger.info("  subfolder in the location : {}", subfolder);
				dto.setCompanyName(subfolder);
				dto.setPromptType(promptType);
				dto.setSourcePath(pathSource + subfolder + "/" + destinationFolderUnprocessed);
				dto.setDstPath(dstPath + subfolder + "/");
				modelService.getOcrData(dto);
			}
		} else {
			logger.info(" There is no subfolder in the location ");
		}

	}

	/*
	 * @Scheduled(fixedDelayString = "${mqtt.received.message.time}") public void
	 * kronheScheduler() { logger.info(" Scheduler Started"); Dto dto = new Dto();
	 * dto.setCompanyName(companyName); dto.setPromptType(promptType);
	 * dto.setSourcePath(pathSource); dto.setDstPath(dstPath);
	 * logger.info(" Source Path : {}",pathSource);
	 * logger.info(" Dst Path : {}",dstPath);
	 * logger.info(" companyName : {}",companyName); modelService.getOcrData(dto); }
	 */

	public static List<String> getSubfolders(String sourcePath,String unprocessedFolderPath) {
		List<String> subfolders = new ArrayList<>();

		File sourceFolder = new File(sourcePath);

		if (sourceFolder.exists() && sourceFolder.isDirectory()) {
			File[] subfolderFiles = sourceFolder.listFiles(File::isDirectory);

			if (subfolderFiles != null) {
				for (File subfolder : subfolderFiles) {
					 String newPath = subfolder.getPath() + "/" + unprocessedFolderPath;
		                File unprocessedFolder = new File(newPath);

		                if (unprocessedFolder.exists() && unprocessedFolder.isDirectory()) {
		                    File[] filesInUnprocessed = unprocessedFolder.listFiles();
		                    if (filesInUnprocessed != null && filesInUnprocessed.length > 0) {
		                        subfolders.add(subfolder.getName());
		                    }
		                }
				}
			}
		}

		return subfolders;
	}

}
