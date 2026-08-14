package com.example.polls.security;

import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.*;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.*;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;


@Configuration
public class JwtConfig {
	@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUrl;
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;
    private static final Logger logger = LoggerFactory.getLogger(JwtConfig.class);
    @Bean
    JwtDecoder jwtDecoder(){
        NimbusJwtDecoder jwtDecoder= NimbusJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .cache(new ConcurrentMapCache("jwk-set"))
                .build();

        OAuth2TokenValidator<Jwt> validator= new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuerUrl),
                new AudienceValidator("PollApplication"),
                new JwtTimestampValidator(Duration.ofSeconds(60))
        );
        jwtDecoder.setJwtValidator(validator);
        return jwtDecoder;
    }


    private static class AudienceValidator implements OAuth2TokenValidator<Jwt>{
        private final List<String> allowedAudiences;
        private static final OAuth2Error INVALID_AUD = new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN,
                "The required audience is missing",
                null );

        @Override
        public OAuth2TokenValidatorResult validate(Jwt jwt) {
           List<String> audiences= jwt.getAudience();
             if (audiences!=null && allowedAudiences.stream().anyMatch(audiences::contains)){
                 return OAuth2TokenValidatorResult.success();
             }
             String azp=jwt.getClaimAsString("azp");
             if(azp != null && allowedAudiences.contains(azp))
                 return OAuth2TokenValidatorResult.success();

             return  OAuth2TokenValidatorResult.failure(INVALID_AUD);
           }
        public AudienceValidator(String... audiences) {
            this.allowedAudiences = Arrays.asList(audiences);
        }
    }


}
