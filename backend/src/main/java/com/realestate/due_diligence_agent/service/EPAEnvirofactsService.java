package com.realestate.due_diligence_agent.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.realestate.due_diligence_agent.exception.ExternalApiException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class EPAEnvirofactsService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public EPAEnvirofactsService(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${EPA_ENVIROFACTS_BASE_URL}") String baseUrl) {

        System.out.println("EPA BASE URL = " + baseUrl);

        this.objectMapper = objectMapper;

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    // =========================================================
    // SEARCH EPA BY ADDRESS
    // =========================================================

    public JsonNode searchFacilities(
            String address,
            String city,
            String state,
            String zipCode,
            String programAcronym) {

        try {

            String rawResponse =
                    webClient.get()
                            .uri(uriBuilder -> {

                                uriBuilder
                                        .queryParam("output", "JSON")
                                        .queryParam("program_output", "yes");

                                if (address != null &&
                                        !address.isBlank()) {

                                    uriBuilder.queryParam(
                                            "street_address",
                                            address
                                    );
                                }

                                if (city != null &&
                                        !city.isBlank()) {

                                    uriBuilder.queryParam(
                                            "city_name",
                                            city
                                    );
                                }

                                if (state != null &&
                                        !state.isBlank()) {

                                    uriBuilder.queryParam(
                                            "state_abbr",
                                            state
                                    );
                                }

                                if (zipCode != null &&
                                        !zipCode.isBlank()) {

                                    uriBuilder.queryParam(
                                            "zip_code",
                                            zipCode
                                    );
                                }

                                if (programAcronym != null &&
                                        !programAcronym.isBlank()) {

                                    uriBuilder.queryParam(
                                            "pgm_sys_acrnm",
                                            programAcronym
                                    );
                                }

                                return uriBuilder.build();
                            })
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

            System.out.println(
                    "========== EPA ADDRESS RAW RESPONSE =========="
            );

            System.out.println(rawResponse);

            System.out.println(
                    "=============================================="
            );

            if (rawResponse == null ||
                    rawResponse.isBlank()) {

                return null;
            }

            return objectMapper.readTree(rawResponse);

        } catch (Exception exception) {

            exception.printStackTrace();

            throw new ExternalApiException(
                    "EPA Envirofacts service is currently unavailable",
                    exception
            );
        }
    }
    // =========================================================
// PAGINATED SEARCH EPA BY ADDRESS
// =========================================================

public JsonNode searchFacilitiesPage(
        String address,
        String city,
        String state,
        String zipCode,
        String programAcronym,
        int limit,
        int offset) {

    try {

        String rawResponse =
                webClient.get()
                        .uri(uriBuilder -> {

                            uriBuilder
                                    .queryParam("output", "JSON")
                                    .queryParam("program_output", "yes")
                                    .queryParam("limit", limit)
                                    .queryParam("offset", offset);

                            if (address != null &&
                                    !address.isBlank()) {

                                uriBuilder.queryParam(
                                        "street_address",
                                        address
                                );
                            }

                            if (city != null &&
                                    !city.isBlank()) {

                                uriBuilder.queryParam(
                                        "city_name",
                                        city
                                );
                            }

                            if (state != null &&
                                    !state.isBlank()) {

                                uriBuilder.queryParam(
                                        "state_abbr",
                                        state
                                );
                            }

                            if (zipCode != null &&
                                    !zipCode.isBlank()) {

                                uriBuilder.queryParam(
                                        "zip_code",
                                        zipCode
                                );
                            }

                            if (programAcronym != null &&
                                    !programAcronym.isBlank()) {

                                uriBuilder.queryParam(
                                        "pgm_sys_acrnm",
                                        programAcronym
                                );
                            }

                            return uriBuilder.build();
                        })
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

        System.out.println(
                "========== EPA PAGINATED RESPONSE =========="
        );

        System.out.println(
                "limit = " + limit +
                ", offset = " + offset
        );

        System.out.println(rawResponse);

        System.out.println(
                "============================================="
        );

        if (rawResponse == null ||
                rawResponse.isBlank()) {

            return null;
        }

        return objectMapper.readTree(rawResponse);

    } catch (Exception exception) {

        exception.printStackTrace();

        throw new ExternalApiException(
                "EPA Envirofacts paginated search service is currently unavailable",
                exception
        );
    }
}

    // =========================================================
    // SEARCH EPA BY COORDINATES
    // =========================================================

    public JsonNode searchFacilitiesByCoordinates(
            double latitude,
            double longitude,
            double searchRadiusMiles,
            String programAcronym) {

        try {

            String rawResponse =
                    webClient.get()
                            .uri(uriBuilder -> {

                                uriBuilder
                                        .queryParam(
                                                "latitude83",
                                                latitude
                                        )
                                        .queryParam(
                                                "longitude83",
                                                longitude
                                        )
                                        .queryParam(
                                                "search_radius",
                                                searchRadiusMiles
                                        )
                                        .queryParam(
                                                "program_output",
                                                "yes"
                                        )
                                        .queryParam(
                                                "output",
                                                "JSON"
                                        );

                                if (programAcronym != null &&
                                        !programAcronym.isBlank()) {

                                    uriBuilder.queryParam(
                                            "pgm_sys_acrnm",
                                            programAcronym
                                    );
                                }

                                return uriBuilder.build();
                            })
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

            System.out.println(
                    "========== EPA RAW COORDINATE RESPONSE =========="
            );

            System.out.println(rawResponse);

            System.out.println(
                    "================================================="
            );

            if (rawResponse == null ||
                    rawResponse.isBlank()) {

                return null;
            }

            return objectMapper.readTree(rawResponse);

        } catch (Exception exception) {

            exception.printStackTrace();

            throw new ExternalApiException(
                    "EPA Envirofacts coordinate search service is currently unavailable",
                    exception
            );
        }
    }
}