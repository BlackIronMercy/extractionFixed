package com.main.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.common.AESEncryption;
import com.main.common.DataContainer;
import com.main.common.GenerateCaptcha;
import com.main.common.GlobalConstant;
import com.main.config.JwtTokenUtil;
import com.main.model.AuditReport;
import com.main.model.CredentialResponse;
import com.main.model.JWTUserDetails;
import com.main.model.JwtRequest;
import com.main.model.JwtResponse;
import com.main.model.User;
import com.main.model.UserSession;
import com.main.repo.AuditReportRepo;
import com.main.repo.UserRepo;
import com.main.repo.UserSessionRepo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultClaims;

@RestController
@CrossOrigin
public class JwtAuthenticationController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private UserRepo userRepo;

	
	@Autowired
	GenerateCaptcha generateCaptcha;

	@Autowired
	AESEncryption aesEncryption;

	@Autowired
	AuditReportRepo auditReportRepo;

	
	@Autowired
	UserSessionRepo userSessionRepo;
	
	@Value("${crossOrigin}")
	public String crossOrigin;

	@Value("${requiredKeyForDetailAtLoginTime}")
	public String requiredKeyForDetailAtLoginTime;

	private static final Logger logger = LogManager.getLogger(JwtAuthenticationController.class);

	@CrossOrigin(origins = "${crossOrigin}")
	@PostMapping(value = "/authenticate")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) {
		
		try {
			
			String loginFlag=authenticationRequest.getLoginFlag();
			String projectString = "";
			String depString = "";
			String designationString = "";
			// logger.info("X-XSRF-TOKEN {}", csrfToken);
			logger.info("encryCRSFToken...............{}", authenticationRequest.getCsrft());
			String decryptedCsrf = aesEncryption.decrypt2(authenticationRequest.getCsrft());
			logger.info("edecryptCRSFToken...............{}", decryptedCsrf);
			if (authenticationRequest.getCsrf() == null || authenticationRequest.getCsrft() == null
					|| "".equalsIgnoreCase(authenticationRequest.getCsrf())
					|| "".equalsIgnoreCase(authenticationRequest.getCsrft())) {
				return new ResponseEntity<>(GlobalConstant.INVALID_CSRF, HttpStatus.BAD_REQUEST);
			}
			if (!authenticationRequest.getCsrf().equals(decryptedCsrf)) {
				return new ResponseEntity<>(GlobalConstant.INVALID_CSRF, HttpStatus.BAD_REQUEST);
			}
			String decryptedCaptch = aesEncryption.decrypt2(authenticationRequest.getEncCaptcha());
			if (!authenticationRequest.getCaptchaInput().equals(decryptedCaptch)) {
				return ResponseEntity.ok(new JwtResponse(GlobalConstant.INVALID_CAPTCHA, GlobalConstant.MSG_FAILED,loginFlag));
			}

			User authenticatedUserObj = authenticate(authenticationRequest.getUsername(),
					authenticationRequest.getPassword());
			
			final String token = jwtTokenUtil.generateToken(authenticatedUserObj);
			logger.info("jwtToken----------------{}", token);
			
			
			Claims allClaimsFromToken = jwtTokenUtil.getAllClaimsFromToken(token);
			Date expirationTime = allClaimsFromToken.getExpiration();
			
			logger.info("jwtToken expirationTime----------------{}", expirationTime);
			if( expirationTime !=null) {
				
				Optional<UserSession> userSessionOptnl= userSessionRepo.findByUserName(authenticatedUserObj.getUsername());
				UserSession usession=null;
				
				Date currentDate=new Date();
				if(userSessionOptnl.isPresent() && !GlobalConstant.FORCE_LOGIN_FLAG.equalsIgnoreCase(loginFlag)) {
					
					return ResponseEntity.ok(new JwtResponse(GlobalConstant.USER_ALREADY_LOGGED_IN, GlobalConstant.MSG_EXIST,""));
					
				}
				else if(userSessionOptnl.isPresent() && GlobalConstant.FORCE_LOGIN_FLAG.equalsIgnoreCase(loginFlag)) {
					usession=userSessionOptnl.get();
					usession.setJwtToken(token);
					usession.setExpiryDate(expirationTime);
					usession.setModifiedDate(currentDate);
				}
				else {
					usession=new UserSession();
					usession.setCreatedDate(currentDate);
					usession.setModifiedDate(currentDate);
					usession.setUserName(authenticatedUserObj.getUsername());
					usession.setJwtToken(token);
					usession.setExpiryDate(expirationTime);
				}
				userSessionRepo.save(usession);
			}
			
			
			
			
			AuditReport auditReort = new AuditReport();
			auditReort.setUserName(authenticatedUserObj.getUsername());
			auditReort.setRole(authenticatedUserObj.getRolesObj().getRoleName());
			auditReort.setActionType(GlobalConstant.Login_success);
			auditReort.setDateTime(new Date());
			auditReportRepo.save(auditReort);
			return ResponseEntity.ok(new JwtResponse(token, GlobalConstant.MSG_SUCCESS,""));

		} catch (DisabledException e) {
			logger.info(GlobalConstant.USER_DISABLED);
			return ResponseEntity.ok(new JwtResponse(GlobalConstant.INVALID_CREDENTIALS, GlobalConstant.MSG_FAILED,""));
		} catch (BadCredentialsException e) {
			logger.info("INVALID_CREDENTIALS {}", HttpStatus.FORBIDDEN);
			return ResponseEntity.ok(new JwtResponse(GlobalConstant.INVALID_CREDENTIALS, GlobalConstant.MSG_FAILED,""));
		} catch (Exception e) {

			logger.info("Exceptions {}", e.toString());

			if ("USER_NOT_ACTIVE".equalsIgnoreCase(e.getMessage())) {
				CredentialResponse credRes = new CredentialResponse();
				credRes.setError("User not active");
				credRes.setStatus(400);
				credRes.setMessage("User not active");
				return new ResponseEntity<>(credRes, HttpStatus.BAD_REQUEST);
			}

			logger.info("INVALID_CREDENTIALS {}", HttpStatus.FORBIDDEN);
			return ResponseEntity.ok(new JwtResponse(GlobalConstant.INVALID_CREDENTIALS, GlobalConstant.MSG_FAILED,""));
		}

	}

	@CrossOrigin(origins = "${crossOrigin}")
	@RequestMapping(value = "/invalidateToken", method = RequestMethod.POST)
	public ResponseEntity<?> invalidateToken(Principal principal) throws Exception {
		CredentialResponse credRes = new CredentialResponse();
		return new ResponseEntity<>("{\"status\":\"success\"}", HttpStatus.ACCEPTED);
	}

	private User authenticate(String username, String password) throws Exception {
		User user = null;
		List<String> statusList = new ArrayList<>();
		statusList.add(GlobalConstant.ACTIVE_STATUS);
		statusList.add(GlobalConstant.PASSWORD_CHANGE_STATUS);
		user = userRepo.findByUsernameAndStatusIn(username, statusList);
		if (user == null) {
			logger.info("User not found with username: {}", username);
			throw new UsernameNotFoundException(
					"Either User is Disabled or not present in the system with username: " + username);
		}

		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

		return user;

	}

	@CrossOrigin(origins = "${crossOrigin}")
	@GetMapping(value = "/refreshtoken")
	public ResponseEntity<?> refreshtoken(HttpServletRequest request) throws Exception {
		logger.info("claims  {}", request.getAttribute("claims"));
		if (null == request.getAttribute("claims")) {

			return new ResponseEntity<>(GlobalConstant.TOKEN_EXPIRED, HttpStatus.BAD_REQUEST);
		}
		DefaultClaims claims = (DefaultClaims) request.getAttribute("claims");

		Map<String, Object> expectedMap = getMapFromIoJsonwebtokenClaims(claims);
		String token = jwtTokenUtil.doGenerateRefreshToken(expectedMap, expectedMap.get("sub").toString());
		
		ObjectMapper mapper = new ObjectMapper();
		Claims allClaimsFromToken = jwtTokenUtil.getAllClaimsFromToken(token);
		JWTUserDetails userDetails = mapper.convertValue(allClaimsFromToken.get(GlobalConstant.JWTTOKEN_CLAIM),
				JWTUserDetails.class);
	
		logger.info("jwtToken customClaimValue:- {}", userDetails);
		Date expirationTime = allClaimsFromToken.getExpiration();
		logger.info("jwtToken expirationTime----------------{}", expirationTime);
		
		if(userDetails.getUsername() !=null && !"".equalsIgnoreCase(userDetails.getUsername()) &&  expirationTime !=null) {
			Optional<UserSession> userSessionOptnl= userSessionRepo.findByUserName(userDetails.getUsername());
			UserSession usession=null;
			
			Date currentDate=new Date();
			
		   if(userSessionOptnl.isPresent() ) {
				usession=userSessionOptnl.get();
				usession.setJwtToken(token);
				usession.setExpiryDate(expirationTime);
				usession.setModifiedDate(currentDate);
			}
			
			userSessionRepo.save(usession);
		}
		
		return ResponseEntity.ok(new JwtResponse(token, GlobalConstant.MSG_SUCCESS,""));
	}

	public Map<String, Object> getMapFromIoJsonwebtokenClaims(DefaultClaims claims) {
		Map<String, Object> expectedMap = new HashMap<String, Object>();
		for (Entry<String, Object> entry : claims.entrySet()) {
			expectedMap.put(entry.getKey(), entry.getValue());
		}
		return expectedMap;
	}

	@CrossOrigin(origins = "${crossOrigin}")
	@PostMapping(value = "/detailAtLoginTime")
	public ResponseEntity<?> detailAtLoginTime(
			@RequestHeader(value = "Key-for-Authenticate-Api", required = true) String requiredKey,
			HttpServletRequest request, Model model) {
		DataContainer data = new DataContainer();
		try {

			if (requiredKey.equalsIgnoreCase(requiredKeyForDetailAtLoginTime)) {

				DataContainer captcha = generateCaptcha.doGetCaptcha();

				UUID uuid = UUID.randomUUID();
				model.addAttribute("csrf", uuid.toString());
				logger.info("uuid   {}", uuid.toString());
				String encCscfToken = aesEncryption.encrypt(uuid.toString());
				logger.info("enuuid   {}", encCscfToken);
				JwtRequest jwtDetails = new JwtRequest();
				jwtDetails.setCsrf(uuid.toString());
				jwtDetails.setEncCaptcha(captcha.getCaptcha());
				jwtDetails.setCsrft(encCscfToken);
				jwtDetails.setCaptchaImg(captcha.getData());
				data.setData(jwtDetails);

				data.setMsg(GlobalConstant.MSG_SUCCESS);

			} else {
				data.setMsg(GlobalConstant.MSG_ERROR);
			}

		} catch (Exception e) {
			data.setMsg(GlobalConstant.MSG_ERROR);
			logger.info("Exceptions {}", e.toString());
			return ResponseEntity.ok(data);

		}
		return ResponseEntity.ok(data);

	}

	/*
	 * @CrossOrigin(origins = "${crossOrigin}")
	 * 
	 * @PostMapping(value = "/logoutDetail") public ResponseEntity<?> logoutDetail(
	 * 
	 * @RequestHeader(value = "Authorization", required = true) String
	 * authorizationToken, HttpServletRequest request, Model model) { DataContainer
	 * data = new DataContainer(); try { String projectString = ""; String depString
	 * = ""; String designationString = ""; ObjectMapper mapper = new
	 * ObjectMapper(); authorizationToken =
	 * jwtTokenUtil.getPureJWTToken(authorizationToken);
	 * logger.info("pure JWT Token in updateEmailTemplateData {}",
	 * authorizationToken);
	 * 
	 * Claims allClaimsFromToken =
	 * jwtTokenUtil.getAllClaimsFromToken(authorizationToken); JWTUserDetails
	 * userDetails =
	 * mapper.convertValue(allClaimsFromToken.get(GlobalConstant.JWTTOKEN_CLAIM),
	 * JWTUserDetails.class); List<UserMapDetails> userMapList =
	 * userMapDetailsRepo.findByUserIdAndStatus(userDetails.getUserId(),
	 * GlobalConstant.ACTIVE_STATUS); if (!userMapList.isEmpty()) { List<String>
	 * projectList = userMapList.stream() .map(userMapListObj ->
	 * userMapListObj.getProjectMdmEntity().getProject())
	 * .collect(Collectors.toList()); if (projectList != null &&
	 * !projectList.isEmpty()) { projectString =
	 * projectList.stream().collect(Collectors.joining(",")); }
	 * 
	 * depString = userMapList.stream() .map(userMapListObj ->
	 * userMapListObj.getDepartmentMdm().getDepartmentName())
	 * .collect(Collectors.joining(","));
	 * 
	 * designationString = userMapList.stream() .map(userMapListObj ->
	 * userMapListObj.getDesignationMdm().getDesignation())
	 * .collect(Collectors.joining(",")); }
	 * 
	 * AuditReport auditReort = new AuditReport();
	 * auditReort.setUserName(userDetails.getUsername());
	 * auditReort.setRole(userDetails.getRole());
	 * auditReort.setActionType(GlobalConstant.Logout_success);
	 * auditReort.setDateTime(new Date()); auditReort.setProjectName(projectString);
	 * auditReort.setDepartment(depString);
	 * auditReort.setDesignation(designationString);
	 * auditReportRepo.save(auditReort);
	 * 
	 * data.setMsg(GlobalConstant.MSG_SUCCESS);
	 * 
	 * Optional<UserSession> userSessionOptnl=
	 * userSessionRepo.findByUserName(userDetails.getUsername());
	 * 
	 * if(userSessionOptnl.isPresent() &&
	 * userSessionOptnl.get().getJwtToken().equals(authorizationToken)) {
	 * userSessionRepo.delete(userSessionOptnl.get()); }
	 * 
	 * 
	 * }
	 * 
	 * catch (Exception e) { data.setMsg(GlobalConstant.MSG_ERROR);
	 * logger.info("Exceptions {}", e.toString()); return ResponseEntity.ok(data);
	 * 
	 * } return ResponseEntity.ok(data);
	 * 
	 * }
	 */
	
	
	public Date getExpirationTime(String token) {
	    try {
	        Claims claims = Jwts.parser()
	                .parseClaimsJws(token)
	                .getBody();
	        
	        // Get the expiration time as a Date object
	        
	        return claims.getExpiration();
	    } catch (Exception e) {
	        // Handle any exceptions here, such as token validation failures.
	        return null;
	    }
	}
	
	@CrossOrigin(origins = "${crossOrigin}")
	@PostMapping(value = "/validateUser")
	public ResponseEntity<?> validateUser(@RequestBody UserSession userSession) {
		
		Long count=userSessionRepo.countByUserNameAndJwtToken(userSession.getUserName(),userSession.getJwtToken());
			if(count>0) {
				return ResponseEntity.ok(new JwtResponse(GlobalConstant.FALSE, GlobalConstant.FALSE,""));
				
			}else {
				return ResponseEntity.ok(new JwtResponse(GlobalConstant.TRUE, GlobalConstant.TRUE,""));
				
			}
			
	}
	
	
}
