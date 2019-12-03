package com.example.faketaxsi;

import java.util.ArrayList;

class MainReader{
    private IReader driver;
    private IReader vehicle;

    public MainReader(IReader driver, IReader vehicle){
        this.driver = driver;
        this.vehicle = vehicle;
    }

    public String getData(){
        String data = "";
        for(String str : driver.getData())
            data += str;
        for(String str : vehicle.getData())
            data += str;

        return data;
    }
}

class DriverReader implements IReader {
    private Driver driver;

    public DriverReader(Driver driver){
        this.driver = driver;
    }

    public ArrayList<String> getData(){
        ArrayList<String> data = new ArrayList<>();
        data.add("Name: " + driver.getName() + "\n");
        data.add("Surname: " + driver.getSurname() + "\n\n");

        return data;
    }
}

class CarReader implements IReader {
    private Car car;

    public CarReader(Car car){
        this.car = car;
    }

    @Override
    public ArrayList<String> getData(){
        ArrayList<String> data = new ArrayList<>();
        data.add("Number: " + car.getNumber() + "\n");
        data.add("Make: " + car.getMake() + " " + car.getModel() + "\n");
        data.add("Color: " + car.getColor());

        return data;
    }
}

interface IReader{
    ArrayList<String> getData();
}
