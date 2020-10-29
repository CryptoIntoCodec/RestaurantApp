package com.foodbox.app.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created  on 28-08-2017.
 */

public class OrderModel {
    String header;
    List<Order> orders = new ArrayList<>();

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

}
