package com.example.tilastotiedonhaku2;

import java.util.ArrayList;

public class Cardatastorage {
    private static Cardatastorage instance;

    private String city;
    private int year;
    private ArrayList<Cardata> carData;

    private Cardatastorage() {
        carData = new ArrayList<>();
    }

    static public Cardatastorage getInstance() {
        if (instance == null) {
            instance = new Cardatastorage();
        }
        return instance;
    }

    public ArrayList<Cardata> getCardata() {
        return carData;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void clearData() {
        carData.clear();
    }

    public String getCity() {
        return city;
    }

    public int getYear() {
        return year;
    }

    public void addCarData(Cardata carDataItem) {
        carData.add(carDataItem);
    }
}