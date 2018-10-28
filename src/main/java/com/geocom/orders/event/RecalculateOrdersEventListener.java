package com.geocom.orders.event;

import com.geocom.orders.service.OrderService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class RecalculateOrdersEventListener implements ApplicationListener<RecalculateOrdersEvent> {

    @Autowired
    private OrderService orderService;

    private final Logger logger = Logger.getLogger(RecalculateOrdersEventListener.class);

    @Override
    public void onApplicationEvent(RecalculateOrdersEvent recalculateOrdersEvent) {
        logger.info("New event found for " + recalculateOrdersEvent.getOrderIds() + " orders");
        orderService.recalculateOrders(recalculateOrdersEvent.getOrderIds());
    }
}
