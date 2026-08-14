package com.example.polls.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtBearerTokenAuthenticationConverter;

import java.util.*;

@Configuration
public class keycloakJwtConfig {

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> keycloackAuthenticationConverter(){
        JwtAuthenticationConverter keycloakAuthenticationConverter= new JwtAuthenticationConverter();
        keycloakAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakGrantedAuthorityConverter());
        return keycloakAuthenticationConverter;
    }
    static class KeycloakGrantedAuthorityConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        private static final String REALM_ACCESS_CLAIM = "realm_access";
        private static final String RESOURCE_ACCESS_CLAIM = "resource_access";
        private static final String ROLES_KEY = "roles";
        private static final String CLIENT_ID = "PollApplication";

        @Override
        @SuppressWarnings("unchecked")
        public Collection<GrantedAuthority> convert(Jwt jwt) {

            Set<GrantedAuthority> authorities=new HashSet<>();
            ///extract realm roles
            Map<String,Object> realmAccess=jwt.getClaimAsMap(REALM_ACCESS_CLAIM);
            if(realmAccess!=null && realmAccess.containsKey(ROLES_KEY)){
                List<String> roles= (List<String>) realmAccess.get(ROLES_KEY);
                roles.forEach(role->authorities.add(new SimpleGrantedAuthority("ROLE_"+role.toUpperCase()))
                );
            }
            ////extract resource/client roles
           Map<String,Object> ResourceAccess=jwt.getClaimAsMap(RESOURCE_ACCESS_CLAIM);
            if(ResourceAccess!=null && ResourceAccess.containsKey(CLIENT_ID)){
                Map<String, Object> clientAccess = (Map<String, Object>) ResourceAccess.get(CLIENT_ID);
                 if (clientAccess!=null && clientAccess.containsKey(ROLES_KEY)){
                     List<String> resourceRoles= (List<String>) clientAccess.get(ROLES_KEY);
                     resourceRoles.forEach(role->authorities.add(new SimpleGrantedAuthority(CLIENT_ID+"_"+role.toUpperCase()))
                     );
                 }
            }
            return authorities;
        }
    }
}
