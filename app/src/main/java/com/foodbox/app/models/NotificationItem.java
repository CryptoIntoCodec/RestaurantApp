package com.foodbox.app.models;

/**
 * Created  on 30-08-2017.
 */

public class NotificationItem {

    public String offerDescription, offerCode, validity;

    public NotificationItem(String name, String price, String validity) {
        this.offerDescription = name;
        this.offerCode = price;
        this.validity = validity;
    }
}
