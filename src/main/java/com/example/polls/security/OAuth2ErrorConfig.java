package com.example.polls.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
public class OAuth2ErrorConfig {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2ErrorConfig.class);

	@Bean
	public AuthenticationEntryPoint customAuthenticationEntryPoint() {
		return ( request,  response, authException)->{
			new BearerTokenAuthenticationEntryPoint().commence(request,  response, authException);
			logger.error("Responding with unauthorized error. Message - {}", authException.getMessage());
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED,authException.getMessage()
			);
		};

	}

	@Bean
	public AccessDeniedHandler customAccessDeniedHandler() {
		return ( request,  response, accessDeniedException)->{
			new BearerTokenAccessDeniedHandler().handle(request,  response, accessDeniedException);
			logger.error("Responding with FORBIDDEN error. Message - {}", accessDeniedException.getMessage());
			response.sendError(HttpServletResponse.SC_FORBIDDEN,accessDeniedException.getMessage()
			);
		};

	}

}
