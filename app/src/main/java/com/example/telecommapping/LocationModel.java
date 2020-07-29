package com.example.telecommapping;

public class LocationModel {
    public double lat, lng;
    public String radio;
    int range;


    public LocationModel(double lat, double lng, String radio){
        this.lat = lat;
        this.lng = lng;
        this.radio = radio;
    }
}
