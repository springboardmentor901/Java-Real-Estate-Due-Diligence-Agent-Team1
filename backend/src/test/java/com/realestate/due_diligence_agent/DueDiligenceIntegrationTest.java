package com.realestate.due_diligence_agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.realestate.due_diligence_agent.client.*;
import com.realestate.due_diligence_agent.dto.AttomBasicProfileResponse;
import com.realestate.due_diligence_agent.dto.DueDiligenceResponse;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.entity.UtilityInformation;
import com.realestate.due_diligence_agent.exception.ExternalApiException;
import com.realestate.due_diligence_agent.repository.PropertyRepository;
import com.realestate.due_diligence_agent.repository.UtilityInformationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DueDiligenceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UtilityInformationRepository utilityInformationRepository;

    @MockBean
    private AttomClient attomClient;

    @MockBean
    private RegridClient regridClient;

    @MockBean
    private FloodZoneClient floodZoneClient;

    @MockBean
    private PermitsClient permitsClient;

    @MockBean
    private EnvironmentalClient environmentalClient;

    @Autowired
    private ObjectMapper objectMapper;

    private Property testProperty;

    @BeforeEach
    void setUp() {
        propertyRepository.deleteAll();
        utilityInformationRepository.deleteAll();

        testProperty = Property.builder()
                .address("123 Test St, Test City, TX 12345")
                .latitude(new BigDecimal("32.7767"))
                .longitude(new BigDecimal("-96.7970"))
                .build();
        testProperty = propertyRepository.save(testProperty);
    }

    @Test
    @WithMockUser
    void testGetDueDiligence_Success() throws Exception {
        // Mock external clients returning empty/valid responses instead of throwing exceptions
        when(attomClient.getDetailOwner(anyString())).thenReturn(null);
        when(attomClient.getAssessment(anyString())).thenReturn(null);
        when(regridClient.getParcelInfo(anyString(), anyString())).thenReturn(null);
        when(floodZoneClient.getFloodZoneData(anyString(), anyString())).thenReturn(null);
        when(permitsClient.getPermits(anyString())).thenReturn(null);
        when(environmentalClient.getEnvironmentalRecords(anyString())).thenReturn(null);

        // Add a utility manually to test it
        UtilityInformation util = UtilityInformation.builder()
                .property(testProperty)
                .utilityType("Water")
                .provider("City Water")
                .status("Active")
                .source("manual")
                .build();
        utilityInformationRepository.save(util);

        MvcResult result = mockMvc.perform(get("/api/properties/" + testProperty.getId() + "/due-diligence"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        DueDiligenceResponse response = objectMapper.readValue(content, DueDiligenceResponse.class);

        assertNotNull(response);
        assertNotNull(response.getOwnership());
        assertNotNull(response.getTaxHistory());
        assertNotNull(response.getZoning());
        assertNotNull(response.getFloodZone());
        assertNotNull(response.getPermits());
        assertNotNull(response.getEnvironmental());
        
        // Utilities should contain the one we saved
        assertNotNull(response.getUtilities());
        assertEquals(1, response.getUtilities().size());
        assertEquals("Water", response.getUtilities().get(0).getUtilityType());
    }

    @Test
    @WithMockUser
    void testGetDueDiligence_PartialFailure() throws Exception {
        // Mock ATTOM to throw an exception
        when(attomClient.getDetailOwner(anyString())).thenThrow(new ExternalApiException("ATTOM failed", null));
        
        // Mock others to return null/empty (success)
        when(attomClient.getAssessment(anyString())).thenReturn(null);
        when(regridClient.getParcelInfo(anyString(), anyString())).thenReturn(null);
        when(floodZoneClient.getFloodZoneData(anyString(), anyString())).thenReturn(null);
        when(permitsClient.getPermits(anyString())).thenReturn(null);
        when(environmentalClient.getEnvironmentalRecords(anyString())).thenReturn(null);

        MvcResult result = mockMvc.perform(get("/api/properties/" + testProperty.getId() + "/due-diligence"))
                .andExpect(status().isOk()) // still returns 200 OK
                .andReturn();

        String content = result.getResponse().getContentAsString();
        DueDiligenceResponse response = objectMapper.readValue(content, DueDiligenceResponse.class);

        assertNotNull(response);
        
        // Ownership should be empty because it failed
        assertTrue(response.getOwnership().isEmpty());
        
        // Other sections should be present (empty lists, but not null and endpoint didn't crash)
        assertNotNull(response.getTaxHistory());
        assertNotNull(response.getZoning());
        assertNotNull(response.getFloodZone());
        assertNotNull(response.getPermits());
        assertNotNull(response.getEnvironmental());
        assertNotNull(response.getUtilities());
    }
}
