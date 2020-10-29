package com.foodbox.app.models;

/**
 * Created  on 28-08-2017.
 */

public class PaymentMethod {
    public String name;
    public Integer icon_id;

    public PaymentMethod(String name, Integer icon_id) {
        this.name = name;
        this.icon_id = icon_id;
    }
}


// icon_id [debit_card = 0 | by_cash = 1]