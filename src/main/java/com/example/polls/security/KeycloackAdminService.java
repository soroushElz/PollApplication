package com.example.polls.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeycloackAdminService {

    @Value("${keycloak.auth-server-url:http://localhost:8080}")
    private String keycloakUrl;

    @Value("${keycloak.realm:springbootApplication}")
    private String realm;

    @Value("${keycloak.admin.client-id:admin-cli}")
    private String adminClientId;

    @Value("${keycloak.admin.client-secret}")
    private String adminClientSecret;
    private final RestTemplate restTemplate;


    public KeycloackAdminService() {
        this.restTemplate = new RestTemplate();
    }

    private String getAdminToken(){
      String tokenUrl=keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";
      HttpHeaders headers=new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      MultiValueMap<String,String> body=new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", adminClientId);
        body.add("client_secret", adminClientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(
                tokenUrl, request, Map.class
        );

        return (String) response.get("access_token");

    }

    public String createUser(String username, String email, String password) {
        String token = getAdminToken();
        String usersUrl = keycloakUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // User representation
        Map<String, Object> user = Map.of(
                "username", username,
                "email", email,
                "enabled", true,
                "emailVerified", true,
                "credentials", List.of(
                        Map.of(
                                "type", "password",
                                "value", password,
                                "temporary", false
                        )
                )
        );
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(user, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                usersUrl, request, Void.class
        );

        String location = response.getHeaders().getLocation().toString();
        return location.substring(location.lastIndexOf("/") + 1);
    }

    public void assignRole(String userId,String roleName){
        String token=getAdminToken();
        String roleUrl = keycloakUrl + "/admin/realms/" + realm + "/roles/" + roleName;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request =new HttpEntity<>(headers);

        @SuppressWarnings("unchecked")
        Map<String,Object> roleRepresentation=restTemplate.exchange(
                roleUrl,
                HttpMethod.GET,
                request,
                Map.class
        ).getBody();

        ///assign role to user
        String roleMappingUrl= keycloakUrl+"/admin/realms/"+realm+"/users/"+userId+"/role-mappings/realm";
        headers.setContentType(MediaType.APPLICATION_JSON);
        assert roleRepresentation != null;
        HttpEntity<List<Map<String,Object>>> assignRoleRequest=
                new HttpEntity<>(List.of(roleRepresentation),headers);

        restTemplate.postForEntity(roleMappingUrl,assignRoleRequest,void.class);


    }



}
