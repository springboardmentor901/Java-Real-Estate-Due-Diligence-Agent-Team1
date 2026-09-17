package com.realestate.due_diligence_agent.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.realestate.due_diligence_agent.entity.ComparableListing;
import com.realestate.due_diligence_agent.service.ComparableListingService;
import com.realestate.due_diligence_agent.service.MarketTrendService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class ComparableListingController {

    private final ComparableListingService comparableListingService;
    private final MarketTrendService marketTrendService;

    @GetMapping("/{id}/comparables")
    public ResponseEntity<List<ComparableListing>> getComparables(
            @PathVariable Long id,
            @RequestParam(defaultValue = "distance") String sortBy) {
                System.out.println("COMPARABLE ENDPOINT REACHED");

        List<ComparableListing> listings =
        new java.util.ArrayList<>(
                comparableListingService.getComparables(id)
        );

        sortListings(listings, sortBy);

        return ResponseEntity.ok(listings);
    }

    @GetMapping("/{id}/comparables/trends")
    public ResponseEntity<MarketTrendService.MarketTrend> getMarketTrends(
            @PathVariable Long id) {
                

        return ResponseEntity.ok(
                marketTrendService.getMarketTrend(id)
        );
    }

    private void sortListings(
            List<ComparableListing> listings,
            String sortBy) {

        switch (sortBy.toLowerCase()) {

            case "price":
                listings.sort(
                        Comparator.comparing(
                                ComparableListing::getPrice,
                                Comparator.nullsLast(Double::compareTo)
                        )
                );
                break;

            case "listeddate":
            case "listed_date":
                listings.sort(
                        Comparator.comparing(
                                ComparableListing::getListedDate,
                                Comparator.nullsLast(java.time.LocalDateTime::compareTo)
                        ).reversed()
                );
                break;

            case "distance":
            default:
                listings.sort(
                        Comparator.comparing(
                                ComparableListing::getDistanceMiles,
                                Comparator.nullsLast(Double::compareTo)
                        )
                );
                break;
        }
    }
}