package com.wayos.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.wayos.Configuration;
import com.wayos.command.admin.AdminCommandNode;
import com.wayos.util.SignatureValidator;

public abstract class AuthorizationFilter implements Filter {
	
	private final List<String> protectURIList = Arrays.asList(
			"libs/",
			"public/",
			"props/",
			"facebook/",
			"line/",
			"account/",
			"keywords/",
			"vars/",
			"context/",
			"csv/",
			"push/");
		
	protected void validateSessionAccountId(HttpServletRequest req, String resourcePath) {
		
		String accountId = (String) req.getSession().getAttribute("accountId");
		
		if (accountId==null || !resourcePath.startsWith(accountId)) {
			
	        throw new AdminCommandNode.AuthenticationException("Invalid Account for " + accountId + " for path " + resourcePath);
		}
		
	}
	
	protected void validateHeaderSignature(HttpServletRequest req, String resourcePath) {
		
		String signature = (String) req.getHeader("Brainy-Signature");
		
		SignatureValidator signatureValidator = new SignatureValidator(Configuration.brainySecret.getBytes());

		/**
		 * Signature Validation
		 * TODO: Test Again!!!
		 */
		if (signature==null || !signatureValidator.validateSignature(resourcePath.getBytes(), signature)) {
			
			String sessionAccountId = (String) req.getSession().getAttribute("accountId");

			throw new AdminCommandNode.AuthenticationException("Invalid Signature for Brainy Admin Command." + (sessionAccountId!=null?"May be attacked from " + sessionAccountId:"") + " for path " + resourcePath); 
		}
		
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest) request;
		
		HttpServletResponse resp = (HttpServletResponse) response;
		
		System.out.println("User-Agent" + req.getHeader("user-agent"));		
				
		HttpSession session = req.getSession();
		
		String accountId = (String) session.getAttribute("accountId");
		
		if (accountId==null) {
			
			//resp.sendRedirect("https://" + req.getServerName());
			
			//return;
			
	        throw new AdminCommandNode.AuthenticationException("Please Login!");
	        
		}
		
		/**
		 * Validate session account Id to protect attack from another account
		 */
		String requestURI = req.getRequestURI();
						
		String resourcePath = "";
		
		for (String protectURI:protectURIList) {
			
			if (requestURI.contains(protectURI)) {
				
				resourcePath = requestURI.substring(requestURI.indexOf(protectURI) + protectURI.length());
				
				try {
					
					validateSessionAccountId(req, resourcePath);
					
				} catch (Exception e) {
					
					validateHeaderSignature(req, resourcePath);
					
				}		
				
				break;
			}
			
		}		
		
		req.setCharacterEncoding("UTF-8");
						
		chain.doFilter(req, resp);
				
	}
	
	@Override
	public void init(FilterConfig fConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

}
