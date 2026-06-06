package com.verbrix.security.service.helpers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import ua_parser.Client;
import ua_parser.Parser;

@Component
public class DeviceMetadataHelper {

    private final Parser uaParser = new Parser();
    private final RestTemplate restTemplate = new RestTemplate();

    public String extractIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public String extractDeviceDetails(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        Client client = uaParser.parse(userAgent);
        return String.format("%s on %s", client.userAgent.family, client.os.family);
    }


    public String getCityAndCountry(String ip) {
        try {
            String url = "http://ip-api.com/json/" + ip;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && "success".equals(response.get("status"))) {
                return String.format("%s, %s", response.get("city"), response.get("country"));
            }
        } catch (Exception e) {
            // Log error or handle failure
        }
        return "Unknown Location";
    }
}