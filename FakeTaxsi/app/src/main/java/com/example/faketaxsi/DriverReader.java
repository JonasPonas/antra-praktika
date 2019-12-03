package com.example.faketaxsi;

import java.util.ArrayList;

public class DriverReader {
    private Driver driver;
    private CarReader carReader;

    public DriverReader(Driver driver){
        this.driver = driver;
        carReader = new CarReader(driver.getCar());
    }

    public String getDriverData(){
        String data = new String();
        data += "Name: " + driver.getName() + "\n";
        data += "Surname: " + driver.getSurname() + "\n\n";
        for(String str : carReader.getCarData())
            data += str;

        return data;
    }
}

class CarReader {
    private Car car;

    public CarReader(Car car){
        this.car = car;
    }

    public ArrayList<String> getCarData(){
        ArrayList<String> data = new ArrayList<>();
        data.add("Number: " + car.getNumber() + "\n");
        data.add("Make: " + car.getMake() + " " + car.getModel() + "\n");
        data.add("Color: " + car.getColor());

        return data;
    }
}
