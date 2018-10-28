package com.geocom.orders.service.impl;

import com.geocom.orders.api.FixerDTO;
import com.geocom.orders.service.FixerService;
import com.geocom.orders.service.cache.FixerCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class FixerServiceImpl implements FixerService {

    @Value("${fixer.api.base.endpoint}")
    private String baseUrl;

    @Value("${fixer.api.access_key}")
    private String accessKey;

    @Value("${app.currency.supported.collection}")
    private String supportedCurrencies;

    private FixerCache fixerCache;

    @Autowired
    public FixerServiceImpl(FixerCache fixerCache) {
        this.fixerCache = fixerCache;
    }

    public BigDecimal convert(String baseCurrency, String destCurrency, BigDecimal value) {
        FixerDTO fixerResponse = fixerCache.callApi(baseUrl, getParams());
        Map<String, BigDecimal> rates = fixerResponse.getRates();
        BigDecimal result = value.divide(rates.get(baseCurrency),2, RoundingMode.HALF_UP);
        BigDecimal total = result.multiply(rates.get(destCurrency)).setScale(2, RoundingMode.HALF_UP);
        return total;
    }

    private Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        params.put("access_key", accessKey);
        params.put("symbols", supportedCurrencies);
        return params;
    }
}
