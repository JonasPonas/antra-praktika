package com.example.faketaxsi;

public class RouteDetails {
    private float price;
    private float distance;
    private float duration;

    public RouteDetails(float price, float distance, float duration){
        this.price = price;
        this.distance = distance;
        this.duration = duration;
    }

    public float getDistance() {
        return distance;
    }
    public float getDuration() {
        return duration;
    }
    public float getPrice() {
        return price;
    }
}
