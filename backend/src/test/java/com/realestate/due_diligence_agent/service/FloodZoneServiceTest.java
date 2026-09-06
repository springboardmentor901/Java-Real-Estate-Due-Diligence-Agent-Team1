package com.realestate.due_diligence_agent.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.realestate.due_diligence_agent.entity.FloodZoneData;
import com.realestate.due_diligence_agent.entity.Property;
import com.realestate.due_diligence_agent.repository.FloodZoneDataRepository;
import com.realestate.due_diligence_agent.repository.PropertyRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class FloodZoneServiceTest {

    @Mock
    private FloodZoneDataRepository floodZoneDataRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private FemaService femaService;

    @Mock
    private ElevationService elevationService;

    @InjectMocks
    private FloodZoneService floodZoneService;

    @Test
    void shouldParseAndSaveMockFemaFloodZoneData() throws Exception {

        // ---------------------------------------------------------
        // 1. Create test property
        // ---------------------------------------------------------

        Property property = new Property();

        property.setId(7L);
        property.setAddress("1200 INDIANA AVE, INDIANAPOLIS, IN");
        property.setLatitude(39.791209);
        property.setLongitude(-86.180209);


        // ---------------------------------------------------------
        // 2. Mock database lookup
        // ---------------------------------------------------------

        when(floodZoneDataRepository.findByPropertyId(7L))
                .thenReturn(Optional.empty());

        when(propertyRepository.findById(7L))
                .thenReturn(Optional.of(property));


        // ---------------------------------------------------------
        // 3. Mock FEMA Flood Zone response
        // ---------------------------------------------------------

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode floodResponse = objectMapper.readTree("""
        {
          "features": [
            {
              "attributes": {
                "FLD_ZONE": "AE",
                "ZONE_SUBTY": "AREA OF SPECIAL FLOOD HAZARD",
                "SFHA_TF": "T",
                "STATIC_BFE": "10.0"
              }
            }
          ]
        }
        """);


        // ---------------------------------------------------------
        // 4. Mock FEMA FIRM Panel response
        // ---------------------------------------------------------

        JsonNode panelResponse = objectMapper.readTree("""
        {
          "features": [
            {
              "attributes": {
                "FIRM_PAN": "18097C0123F"
              }
            }
          ]
        }
        """);


        when(femaService.getFloodZone(
                39.791209,
                -86.180209))
                .thenReturn(floodResponse);

        when(femaService.getFirmPanel(
                39.791209,
                -86.180209))
                .thenReturn(panelResponse);


        // ---------------------------------------------------------
        // 5. Mock elevation
        // ---------------------------------------------------------

        when(elevationService.getElevation(
                39.791209,
                -86.180209))
                .thenReturn(150.5);


        // ---------------------------------------------------------
        // 6. Mock database save
        // ---------------------------------------------------------

        when(floodZoneDataRepository.save(
                any(FloodZoneData.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));


        // ---------------------------------------------------------
        // 7. Execute service
        // ---------------------------------------------------------

        FloodZoneData result =
                floodZoneService.getFloodZoneData(7L);


        // ---------------------------------------------------------
        // 8. Verify result
        // ---------------------------------------------------------

        assertNotNull(result);

        assertEquals(
                "AE",
                result.getFloodZone()
        );

        assertEquals(
                "HIGH",
                result.getFloodRiskRating()
        );

        assertEquals(
                "18097C0123F",
                result.getFemaMapPanel()
        );

        assertEquals(
                BigDecimal.valueOf(150.5),
                result.getElevationData()
        );

        assertEquals(
                property,
                result.getProperty()
        );

        assertNotNull(
                result.getRetrievedAt()
        );


        // ---------------------------------------------------------
        // 9. Verify APIs were called
        // ---------------------------------------------------------

        verify(femaService)
                .getFloodZone(
                        39.791209,
                        -86.180209
                );

        verify(femaService)
                .getFirmPanel(
                        39.791209,
                        -86.180209
                );

        verify(elevationService)
                .getElevation(
                        39.791209,
                        -86.180209
                );


        // ---------------------------------------------------------
        // 10. Verify database save
        // ---------------------------------------------------------

        verify(floodZoneDataRepository)
                .save(any(FloodZoneData.class));
    }
}