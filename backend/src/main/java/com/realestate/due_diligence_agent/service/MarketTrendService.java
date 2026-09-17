package com.realestate.due_diligence_agent.service;

import com.realestate.due_diligence_agent.entity.ComparableListing;
import com.realestate.due_diligence_agent.repository.ComparableListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketTrendService {

    private final ComparableListingRepository comparableListingRepository;

    public MarketTrend getMarketTrend(Long propertyId) {

        List<ComparableListing> listings =
                comparableListingRepository.findByPropertyId(propertyId);

        if (listings.isEmpty()) {
            return new MarketTrend(0.0, 0.0, "NO_DATA");
        }

        double averagePrice = listings.stream()
                .map(ComparableListing::getPrice)
                .filter(price -> price != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double averagePricePerSquareFoot = listings.stream()
                .filter(listing -> listing.getPrice() != null)
                .filter(listing -> listing.getSquareFeet() != null)
                .filter(listing -> listing.getSquareFeet() > 0)
                .mapToDouble(listing ->
                        listing.getPrice() / listing.getSquareFeet())
                .average()
                .orElse(0.0);

        String trend = calculateRecencyWeightedTrend(listings);

        return new MarketTrend(
                averagePrice,
                averagePricePerSquareFoot,
                trend
        );
    }

    private String calculateRecencyWeightedTrend(
            List<ComparableListing> listings) {

        LocalDateTime now = LocalDateTime.now();

        double recentWeight = 0.0;
        double olderWeight = 0.0;

        for (ComparableListing listing : listings) {

            if (listing.getListedDate() == null) {
                continue;
            }

            long daysOld = java.time.Duration.between(
                    listing.getListedDate(),
                    now
            ).toDays();

            if (daysOld <= 30) {
                recentWeight += 2.0;
            } else if (daysOld <= 90) {
                recentWeight += 1.0;
            } else {
                olderWeight += 1.0;
            }
        }

        if (recentWeight > olderWeight) {
            return "RISING";
        }

        if (recentWeight < olderWeight) {
            return "STABLE_OR_DECLINING";
        }

        return "STABLE";
    }

    public record MarketTrend(
            double averagePrice,
            double averagePricePerSquareFoot,
            String trend
    ) {
    }
}