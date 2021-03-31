package com.svalero.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Country {

    private String name;
    private String capital;
    private String region;
    private String subregion;
    private String flag;
    private long population;

    @Override
    public String toString() {
        return name + " [" + population + "] ";
    }
}