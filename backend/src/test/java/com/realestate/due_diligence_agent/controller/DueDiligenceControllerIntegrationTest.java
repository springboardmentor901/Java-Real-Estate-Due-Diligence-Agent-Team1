package com.realestate.due_diligence_agent.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.realestate.due_diligence_agent.dto.AuthResponse;
import com.realestate.due_diligence_agent.dto.DueDiligenceResponse;
import com.realestate.due_diligence_agent.dto.LoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class DueDiligenceControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnAllSevenDueDiligenceSections() {

        // ---------------------------------------------------------
        // 1. Login
        // ---------------------------------------------------------

        LoginRequest loginRequest =
                new LoginRequest(
                        "admin@realestateapp.com",
                        "Admin@123"
                );

        ResponseEntity<AuthResponse> loginResponse =
                restTemplate.postForEntity(
                        "/api/auth/login",
                        loginRequest,
                        AuthResponse.class
                );

        assertEquals(
                200,
                loginResponse.getStatusCode().value(),
                "Login failed"
        );

        assertNotNull(
                loginResponse.getBody(),
                "Login response body is null"
        );

        String token =
                loginResponse.getBody().getToken();

        assertNotNull(
                token,
                "JWT token was not returned"
        );


        // ---------------------------------------------------------
        // 2. Create Authorization header
        // ---------------------------------------------------------

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(token);

        HttpEntity<Void> requestEntity =
                new HttpEntity<>(headers);


        // ---------------------------------------------------------
        // 3. Call due-diligence endpoint
        // ---------------------------------------------------------

        String url =
                "http://localhost:"
                        + port
                        + "/api/properties/7/due-diligence";

        ResponseEntity<DueDiligenceResponse> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        requestEntity,
                        DueDiligenceResponse.class
                );


        // ---------------------------------------------------------
        // 4. Verify HTTP response
        // ---------------------------------------------------------

        assertEquals(
                200,
                response.getStatusCode().value(),
                "Due diligence endpoint did not return 200"
        );

        assertNotNull(
                response.getBody(),
                "Due diligence response body is null"
        );


        // ---------------------------------------------------------
        // 5. Verify all seven sections
        // ---------------------------------------------------------

        DueDiligenceResponse result =
                response.getBody();

        assertNotNull(
                result.getOwnership(),
                "Ownership section is missing"
        );

        assertNotNull(
                result.getTaxHistory(),
                "Tax History section is missing"
        );

        assertNotNull(
                result.getZoning(),
                "Zoning section is missing"
        );

        assertNotNull(
                result.getFloodZone(),
                "Flood Zone section is missing"
        );

        assertNotNull(
                result.getPermits(),
                "Permits section is missing"
        );

        assertNotNull(
                result.getEnvironmental(),
                "Environmental section is missing"
        );

        assertNotNull(
                result.getUtilities(),
                "Utilities section is missing"
        );
    }
}