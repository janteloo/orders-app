package com.geocom.orders.event;

import org.springframework.context.ApplicationEvent;

import java.util.List;

public class RecalculateOrdersEvent extends ApplicationEvent {

    private List<Long> orderIds;

    public RecalculateOrdersEvent(Object source, List<Long> orderIds) {
        super(source);
        this.orderIds = orderIds;
    }

    public List<Long> getOrderIds() {
        return orderIds;
    }
}
