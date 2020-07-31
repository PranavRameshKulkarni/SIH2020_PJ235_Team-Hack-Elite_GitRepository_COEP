package com.example.telecommapping;

public class LocationModel {
    public double lat, lng;
    public String radio, address, name;
    int range;


    public LocationModel(double lat, double lng, String radio){
        this.lat = lat;
        this.lng = lng;
        this.radio = radio;
    }
    public LocationModel(){}

    public LocationModel setLocation(double lat, double lng, String address, String name){
        LocationModel lc = new LocationModel();
        lc.lat = lat;
        lc.lng = lng;
        lc.address = address;
        lc.name = name;
        return lc;
    }


}
