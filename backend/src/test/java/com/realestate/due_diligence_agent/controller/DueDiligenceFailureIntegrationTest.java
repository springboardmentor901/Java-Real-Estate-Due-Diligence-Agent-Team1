package com.realestate.due_diligence_agent.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.realestate.due_diligence_agent.dto.AuthResponse;
import com.realestate.due_diligence_agent.dto.DueDiligenceResponse;
import com.realestate.due_diligence_agent.dto.LoginRequest;
import com.realestate.due_diligence_agent.service.FloodZoneService;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true"
        }
)
@AutoConfigureTestRestTemplate
class DueDiligenceFailureIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FloodZoneService floodZoneService;


    @Test
    void shouldReturnOtherSectionsWhenFloodZoneFails() {

        // ---------------------------------------------------------
        // 1. Make Flood Zone service fail
        // ---------------------------------------------------------

        when(floodZoneService.getFloodZoneData(7L))
                .thenThrow(
                        new RuntimeException(
                                "Flood Zone service unavailable"
                        )
                );


        // ---------------------------------------------------------
        // 2. Login
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

        assertNotNull(loginResponse.getBody());

        String token =
                loginResponse.getBody().getToken();

        assertNotNull(token);


        // ---------------------------------------------------------
        // 3. Authorization
        // ---------------------------------------------------------

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(token);

        HttpEntity<Void> requestEntity =
                new HttpEntity<>(headers);


        // ---------------------------------------------------------
        // 4. Call aggregation endpoint
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
        // 5. HTTP 200
        // ---------------------------------------------------------

        assertEquals(
                200,
                response.getStatusCode().value(),
                "Aggregation should return 200 when one service fails"
        );

        assertNotNull(response.getBody());

        DueDiligenceResponse result =
                response.getBody();


        // ---------------------------------------------------------
        // 6. Flood Zone should be FAILED
        // ---------------------------------------------------------

        assertNotNull(result.getFloodZone());

        assertEquals(
                "FAILED",
                result.getFloodZone().getStatus()
        );


        // ---------------------------------------------------------
        // 7. Other six sections should still exist
        // ---------------------------------------------------------

        assertNotNull(result.getOwnership());
        assertNotNull(result.getTaxHistory());
        assertNotNull(result.getZoning());
        assertNotNull(result.getPermits());
        assertNotNull(result.getEnvironmental());
        assertNotNull(result.getUtilities());
    }


    // -------------------------------------------------------------
    // Test configuration
    // -------------------------------------------------------------

    @TestConfiguration
    static class MockConfiguration {

        @Bean
        FloodZoneService floodZoneService() {

            return mock(FloodZoneService.class);
        }
    }
}