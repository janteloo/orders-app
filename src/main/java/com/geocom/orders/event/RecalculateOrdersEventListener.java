package com.geocom.orders.event;

import com.geocom.orders.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class RecalculateOrdersEventListener implements ApplicationListener<RecalculateOrdersEvent> {

    @Autowired
    private OrderService orderService;

    @Override
    public void onApplicationEvent(RecalculateOrdersEvent recalculateOrdersEvent) {
        orderService.recalculateOrders(recalculateOrdersEvent.getOrderIds());
    }
}
