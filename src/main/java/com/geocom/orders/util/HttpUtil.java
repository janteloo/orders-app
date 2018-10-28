package com.geocom.orders.util;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

public class HttpUtil {

    public static <T> T performGet(String url, Map<String, String> params, Class<T> clazz) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        params.forEach((k,v)-> builder.queryParam(k, v));

        RestTemplate restTemplate = new RestTemplate();
        T response = restTemplate.getForObject(builder.toUriString(), clazz);
        return response;
    }
}
