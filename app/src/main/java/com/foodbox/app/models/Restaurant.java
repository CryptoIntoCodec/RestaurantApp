package com.foodbox.app.models;

/**
 * Created  on 30-08-2017.
 */

public class Restaurant {

    public String name, category, offer, rating, distance, price, restaurantInfo;

    public Restaurant(String name, String category, String offer, String rating, String distance, String price, String restaurantInfo) {
        this.name = name;
        this.category = category;
        this.offer = offer;
        this.rating = rating;
        this.distance = distance;
        this.restaurantInfo = restaurantInfo;
        this.price = price;
    }
}
