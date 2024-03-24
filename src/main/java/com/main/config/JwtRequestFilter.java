package com.main.config;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.main.common.GlobalConstant;
import com.main.exception.ServiceException;
import com.main.model.UserSession;
import com.main.repo.UserSessionRepo;
import com.main.service.JwtUserDetailsService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired 
    private UserSessionRepo userSessionRepo;

    private static final Logger logger = LogManager.getLogger(JwtRequestFilter.class);
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;
        String isRefreshToken = request.getHeader("isRefreshToken");
        String requestURL = request.getRequestURL().toString();
        logger.info("isRefreshToken {}" , isRefreshToken);
        logger.info("requestURL {}" , requestURL);
        try {
            // JWT Token is in the form "Bearer token". Remove Bearer word and get
            // only the Token
            if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
                jwtToken = requestTokenHeader.substring(7);
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
              
                if (isRefreshToken != null && isRefreshToken.equals("true") && requestURL.contains("refreshtoken")) {
                	  if(checkSessionUsername(username, jwtToken)) {
                		  Claims claims = jwtTokenUtil.getAllClaimsFromToken(jwtToken);
                          request.setAttribute("claims", claims);
                	  }
                	  else {
                  		throw new ServiceException(GlobalConstant.Logout_success);
                  	}
                }
                else{
                	 Claims claims = jwtTokenUtil.getAllClaimsFromToken(jwtToken);
                     request.setAttribute("claims", claims);
                }
               
                

            } else {
                logger.warn("JWT Token does not begin with Bearer String");
            }
        } catch (ExpiredJwtException ex) {
            
        	logger.info("Expired JWT token");
            request.setAttribute("expired", ex.getMessage());
            
           
      
         
            // allow for Refresh Token creation if following conditions are true.
            if (isRefreshToken != null && isRefreshToken.equals("true") && requestURL.contains("refreshtoken") ) {
            	if(checkSessionUsername(username, jwtToken)) {
            		allowForRefreshToken(ex, request);
            	}
            	else {
            		throw new ServiceException(GlobalConstant.Logout_success);
            	}
            	
                
            }
        }
//        catch (ExpiredJwtException e) {
//            System.out.println("JWT Token has expired");
//        }
        // Once we get the token validate it.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);

            // if token is valid configure Spring Security to manually set
            // authentication
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // After setting the Authentication in the context, we specify
                // that the current user is authenticated. So it passes the
                // Spring Security Configurations successfully.
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }

    private void allowForRefreshToken(ExpiredJwtException ex, HttpServletRequest request) {

        // create a UsernamePasswordAuthenticationToken with null values.
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                null, null, null);
        // After setting the Authentication in the context, we specify
        // that the current user is authenticated. So it passes the
        // Spring Security Configurations successfully.
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        // Set the claims so that in controller we will be using it to create
        // new JWT
        logger.info("Claims for allowForRefreshToken {}" , ex.getClaims());
//        request.setAttribute("claims", ex.getClaims());

    }

    public boolean checkSessionUsername(String username, String token) {
    	
    	Long count=userSessionRepo.countByUserNameAndJwtToken(username,token);
    	return count > 0;    	
    }
    
}
