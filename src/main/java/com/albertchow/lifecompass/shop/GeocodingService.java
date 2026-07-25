package com.albertchow.lifecompass.shop;

import com.albertchow.lifecompass.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Turns a free-text place name (e.g. "Dundrum", "National College of
 * Ireland") into coordinates, using OpenStreetMap's free Nominatim search API
 * — no API key needed, which is why this was chosen over a paid provider.
 * Results are restricted to Ireland ({@code countrycodes=ie}) since that's
 * the only market this app covers, which also makes ambiguous place names
 * ("Dundrum" also exists elsewhere) resolve sensibly.
 */
@Slf4j
@Service
public class GeocodingService {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://nominatim.openstreetmap.org")
            // Nominatim's usage policy requires a descriptive User-Agent identifying the
            // application for server-to-server requests (a generic Java/RestClient
            // default would be rejected as anonymous automated traffic).
            .defaultHeader("User-Agent", "LifeCompass-StudentProject/1.0 (contact: albertchow1997@gmail.com)")
            .build();

    /**
     * Geocodes a place name to a single best-match point.
     *
     * @throws BusinessException if the place can't be found or the geocoding call fails
     */
    public Point geocode(String place) {
        List<NominatimResult> results;
        try {
            results = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", place)
                            .queryParam("format", "json")
                            .queryParam("limit", 1)
                            .queryParam("countrycodes", "ie")
                            .build())
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<NominatimResult>>() {
                    });
        } catch (RuntimeException e) {
            log.warn("Geocoding call failed for place '{}'", place, e);
            throw new BusinessException("Could not search for that place right now");
        }

        if (results == null || results.isEmpty()) {
            throw new BusinessException("Could not find \"" + place + "\" in Ireland");
        }
        NominatimResult top = results.get(0);
        return new Point(Double.parseDouble(top.lon()), Double.parseDouble(top.lat()));
    }

    /** Minimal shape of a Nominatim search result — only the fields we read. */
    private record NominatimResult(String lat, String lon) {
    }
}
