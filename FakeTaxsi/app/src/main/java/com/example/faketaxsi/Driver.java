package com.example.faketaxsi;

import com.google.android.gms.maps.model.LatLng;

import java.util.Random;

public class Driver {
    private String name;
    private String surname;
    private Car car;
    private LatLng loc;

    public Driver(String name, String surname, String number, String make, String model, String color){
        this.name = name;
        this.surname = surname;
        this.car = new Car(number, make, model, color);

        Random rand = new Random();
        float min = 54.659843f;
        float max = 54.756135f;
        float lat = rand.nextFloat() * (max - min) + min;
        min = 25.195755f;
        max = 25.322694f;
        float lng = rand.nextFloat() * (max - min) + min;
        loc = new LatLng(lat, lng);
    }

    public Car getCar() {
        return car;
    }
    public LatLng getLoc() {
        return loc;
    }
    public String getName() {
        return name;
    }
    public String getSurname() {
        return surname;
    }
}

class Car{
    private String number;
    private String make;
    private String model;
    private String color;

    public Car(String number, String make, String model, String color){
        this.number = number;
        this.make = make;
        this.model = model;
        this.color = color;
    }

    public String getColor() {
        return color;
    }
    public String getMake() {
        return make;
    }
    public String getModel() {
        return model;
    }
    public String getNumber() {
        return number;
    }
}
