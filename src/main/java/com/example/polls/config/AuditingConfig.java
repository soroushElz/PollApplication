package com.example.polls.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.polls.security.UserPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
@EnableJpaAuditing
public class AuditingConfig {

	@Bean
	public AuditorAware<String> auditorProvider(){
		return new SpringSecurityAuditorAwareImpl();
	}
}

 class SpringSecurityAuditorAwareImpl implements AuditorAware<String> {

	@Override
	public Optional<String> getCurrentAuditor() {
		Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
		
		if(authentication==null || 
				 !authentication.isAuthenticated()||
				    authentication instanceof  AnonymousAuthenticationToken) {
			return Optional.empty();

		}

       return Optional.ofNullable(((Jwt)authentication.getPrincipal()).getSubject());
		
	}
	 
 }
 