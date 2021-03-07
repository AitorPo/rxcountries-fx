package com.svalero.domain;


public class Country {

    private String name;
    private String capital;
    private String region;
    private String subregion;

    public Country(String name, String capital, String region, String subregion) {
        this.name = name;
        this.capital = capital;
        this.region = region;
        this.subregion = subregion;
    }

    @Override
    public String toString() {
        return name + " [" + capital +  "]";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSubregion() {
        return subregion;
    }

    public void setSubregion(String subregion) {
        this.subregion = subregion;
    }
}
