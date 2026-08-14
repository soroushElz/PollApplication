package com.example.polls.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;


@EnableWebSecurity
@EnableMethodSecurity(
       securedEnabled = true,
      jsr250Enabled = true
)
@Configuration
public class SecurityConfig {


    @Autowired
	keycloakJwtConfig keycloakJwtConfig;
     
	 @Bean
	SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,
												   AuthenticationEntryPoint authEntryPoint,
												   AccessDeniedHandler accessDeniedHandler) throws Exception {
		 http.csrf(AbstractHttpConfigurer::disable)
		       .cors(AbstractHttpConfigurer::disable)
		       .authorizeHttpRequests((authz) ->
		         authz.requestMatchers("/api/users/checkUsernameAvailability", "/api/users/checkEmailAvailability")
                             .permitAll()
                       .requestMatchers(HttpMethod.POST,"/api/auth/**")
                              .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/polls/**", "/api/users/**")
                               .authenticated()
                        .requestMatchers("/",

                        "/favicon.ico",
                        "/*/*.png",
                        "/*/*.gif",
                        "/*/*.svg",
                        "/*/*.jpg",
                        "/*/*.html",
                        "/*/*.css",
                        "/*/*.js")
                              .permitAll()
                        .anyRequest().authenticated()
                        )
		      .sessionManagement((session) -> session
                 .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				 .oauth2ResourceServer(oauth2->oauth2
						 .authenticationEntryPoint(authEntryPoint)
						 .accessDeniedHandler(accessDeniedHandler)
						 .jwt(jwt ->
								jwt.jwtAuthenticationConverter(keycloakJwtConfig.keycloackAuthenticationConverter())
				 ));
		     return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder(){
		 return new BCryptPasswordEncoder();
	}

}
