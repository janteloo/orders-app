package com.geocom.orders.service.cache;

import com.geocom.orders.api.FixerDTO;
import com.geocom.orders.util.HttpUtil;
import org.apache.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FixerCache {

    private final Logger logger = Logger.getLogger(FixerCache.class);

    @Cacheable("fixerCache")
    public FixerDTO callApi(String url, Map<String, String> params) {
        logger.info("Calling API");
        return HttpUtil.performGet(url, params, FixerDTO.class);
    }
}
