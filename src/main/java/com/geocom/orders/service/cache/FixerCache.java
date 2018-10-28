package com.geocom.orders.service.cache;

import com.geocom.orders.api.FixerDTO;
import com.geocom.orders.util.HttpUtil;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FixerCache {

    @Cacheable("fixerCache")
    public FixerDTO callApi(String url, Map<String, String> params) {
        return HttpUtil.performGet(url, params, FixerDTO.class);
    }
}
