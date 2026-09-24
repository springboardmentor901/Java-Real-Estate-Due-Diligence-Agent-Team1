package com.realestate.due_diligence_agent.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class AddressParser {

    private final String raw;
    private final String street;
    private final String city;
    private final String state;
    private final String zip;
    private final boolean usAddress;
    private final String address1;
    private final String address2;
    private final String rapidApiLocation;

    private static final Map<String, String> US_STATES = Map.ofEntries(
            Map.entry("alabama", "AL"), Map.entry("alaska", "AK"), Map.entry("arizona", "AZ"),
            Map.entry("arkansas", "AR"), Map.entry("california", "CA"), Map.entry("colorado", "CO"),
            Map.entry("connecticut", "CT"), Map.entry("delaware", "DE"), Map.entry("florida", "FL"),
            Map.entry("georgia", "GA"), Map.entry("hawaii", "HI"), Map.entry("idaho", "ID"),
            Map.entry("illinois", "IL"), Map.entry("indiana", "IN"), Map.entry("iowa", "IA"),
            Map.entry("kansas", "KS"), Map.entry("kentucky", "KY"), Map.entry("louisiana", "LA"),
            Map.entry("maine", "ME"), Map.entry("maryland", "MD"), Map.entry("massachusetts", "MA"),
            Map.entry("michigan", "MI"), Map.entry("minnesota", "MN"), Map.entry("mississippi", "MS"),
            Map.entry("missouri", "MO"), Map.entry("montana", "MT"), Map.entry("nebraska", "NE"),
            Map.entry("nevada", "NV"), Map.entry("new hampshire", "NH"), Map.entry("new jersey", "NJ"),
            Map.entry("new mexico", "NM"), Map.entry("new york", "NY"), Map.entry("north carolina", "NC"),
            Map.entry("north dakota", "ND"), Map.entry("ohio", "OH"), Map.entry("oklahoma", "OK"),
            Map.entry("oregon", "OR"), Map.entry("pennsylvania", "PA"), Map.entry("rhode island", "RI"),
            Map.entry("south carolina", "SC"), Map.entry("south dakota", "SD"), Map.entry("tennessee", "TN"),
            Map.entry("texas", "TX"), Map.entry("utah", "UT"), Map.entry("vermont", "VT"),
            Map.entry("virginia", "VA"), Map.entry("washington", "WA"), Map.entry("west virginia", "WV"),
            Map.entry("wisconsin", "WI"), Map.entry("wyoming", "WY"), Map.entry("district of columbia", "DC")
    );

    private static final Pattern STATE_ZIP_PATTERN = Pattern.compile("^([A-Za-z\\s]+)\\s+(\\d{5}(-\\d{4})?)$");
    private static final Pattern ZIP_PATTERN = Pattern.compile("^\\d{5}(-\\d{4})?$");
    private static final Pattern NUMBER_PREFIX_PATTERN = Pattern.compile("^\\d+[A-Za-z]?$");
    private static final Pattern COUNTY_TOWNSHIP_PATTERN = Pattern.compile("(?i)\\b(County|Township|Parish|Distrito|Municipio|Ward)\\b");

    public static AddressParser parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return AddressParser.builder()
                    .raw(raw)
                    .street("")
                    .city("")
                    .state("")
                    .zip("")
                    .usAddress(false)
                    .address1("")
                    .address2("")
                    .rapidApiLocation(null)
                    .build();
        }

        String[] rawParts = raw.split(",");
        List<String> parts = new ArrayList<>();
        for (String p : rawParts) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) {
                parts.add(trimmed);
            }
        }

        if (parts.isEmpty()) {
            return AddressParser.builder()
                    .raw(raw)
                    .street(raw.trim())
                    .city("")
                    .state("")
                    .zip("")
                    .usAddress(false)
                    .address1(raw.trim())
                    .address2("")
                    .rapidApiLocation(null)
                    .build();
        }

        boolean isUs = Pattern.compile("(?i)\\b(United States|USA)\\b").matcher(raw).find();
        String street = "";
        String city = "";
        String state = null;
        String zip = null;

        int streetEndIdx = 0;
        int numIdx = -1;
        for (int idx = 0; idx < parts.size(); idx++) {
            String p = parts.get(idx);
            if (NUMBER_PREFIX_PATTERN.matcher(p).matches()) {
                numIdx = idx;
                break;
            }
            // Check if token already starts with number + street name, e.g. "1825 Montcalm St"
            Matcher m = Pattern.compile("^(\\d+[A-Za-z]?)\\s+(.+)$").matcher(p);
            if (m.matches() && !COUNTY_TOWNSHIP_PATTERN.matcher(p).find() && normalizeState(p) == null) {
                street = p;
                streetEndIdx = idx;
                numIdx = -2;
                break;
            }
        }

        if (numIdx >= 0 && numIdx + 1 < parts.size()) {
            street = parts.get(numIdx) + " " + parts.get(numIdx + 1);
            streetEndIdx = numIdx + 1;
        } else if (numIdx == -1 && street.isEmpty()) {
            street = parts.get(0);
            streetEndIdx = 0;
        }

        int endIdx = parts.size() - 1;
        if (endIdx > streetEndIdx && (parts.get(endIdx).equalsIgnoreCase("United States") || parts.get(endIdx).equalsIgnoreCase("USA"))) {
            isUs = true;
            endIdx--;
        }

        if (endIdx > streetEndIdx && ZIP_PATTERN.matcher(parts.get(endIdx)).matches()) {
            zip = parts.get(endIdx);
            isUs = true;
            endIdx--;
        }

        if (endIdx > streetEndIdx) {
            String candidate = parts.get(endIdx);
            Matcher szm = STATE_ZIP_PATTERN.matcher(candidate);
            if (szm.matches()) {
                String normalizedState = normalizeState(szm.group(1));
                if (normalizedState != null) {
                    state = normalizedState;
                    if (zip == null) {
                        zip = szm.group(2);
                    }
                    isUs = true;
                    endIdx--;
                }
            } else {
                String normalizedState = normalizeState(candidate);
                if (normalizedState != null) {
                    state = normalizedState;
                    isUs = true;
                    endIdx--;
                }
            }
        }

        for (int i = endIdx; i > streetEndIdx; i--) {
            String part = parts.get(i);
            if (COUNTY_TOWNSHIP_PATTERN.matcher(part).find()) {
                continue;
            }
            city = part;
            break;
        }

        if (city.isEmpty() && parts.size() > 1) {
            city = parts.get(1);
        }

        StringBuilder addr2Builder = new StringBuilder();
        if (!city.isEmpty()) {
            addr2Builder.append(city);
        }
        if (state != null && !state.isEmpty()) {
            if (addr2Builder.length() > 0) {
                addr2Builder.append(", ");
            }
            addr2Builder.append(state);
        }
        if (zip != null && !zip.isEmpty()) {
            if (addr2Builder.length() > 0) {
                addr2Builder.append(" ");
            }
            addr2Builder.append(zip);
        }

        String rapidLoc = null;
        if (isUs && !city.isEmpty()) {
            if (state != null && !state.isEmpty()) {
                rapidLoc = "city:" + city + ", " + state;
            } else {
                rapidLoc = "city:" + city;
            }
        }

        return AddressParser.builder()
                .raw(raw)
                .street(street)
                .city(city)
                .state(state != null ? state : "")
                .zip(zip != null ? zip : "")
                .usAddress(isUs)
                .address1(street)
                .address2(addr2Builder.toString().trim())
                .rapidApiLocation(rapidLoc)
                .build();
    }

    public static String normalizeState(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return null;
        }
        String s = candidate.trim();
        if (s.length() == 2) {
            String upper = s.toUpperCase();
            if (US_STATES.containsValue(upper)) {
                return upper;
            }
        }
        return US_STATES.get(s.toLowerCase());
    }
}
