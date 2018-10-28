package com.geocom.orders.services;

import com.geocom.orders.api.FixerDTO;
import com.geocom.orders.service.cache.FixerCache;
import com.geocom.orders.service.impl.FixerServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FixerServiceTest {

    @Mock
    private FixerCache fixerCache;

    private FixerServiceImpl fixerService;

    private FixerDTO fixerDTO;

    @Before
    public void setup() {
        fixerService = new FixerServiceImpl(fixerCache);

        Map<String, BigDecimal> rates = new HashMap<>();
        rates.put("USD", new BigDecimal(1.142446));
        rates.put("UYU", new BigDecimal(37.598312));
        rates.put("EUR", new BigDecimal(1));
        fixerDTO = new FixerDTO();
        fixerDTO.setRates(rates);
    }

    @Test
    public void convert() {
        when(fixerCache.callApi(Mockito.any(), Mockito.any())).thenReturn(fixerDTO);
        BigDecimal returnValue = fixerService.convert("USD", "UYU", new BigDecimal(100));
        assert Math.ceil(returnValue.doubleValue()) == 3291;

        returnValue = fixerService.convert("UYU", "USD", new BigDecimal(1650));
        assert Math.floor(returnValue.doubleValue()) == 50;

        returnValue = fixerService.convert("USD", "EUR", new BigDecimal(100));
        assert Math.ceil(returnValue.doubleValue()) == 88;


        BigDecimal firstConversion = fixerService.convert("UYU", "USD", new BigDecimal(750234));
        BigDecimal secondCoversion = fixerService.convert("USD", "UYU", firstConversion);

        assert Math.floor(secondCoversion.doubleValue()) == 750234;
    }


}
