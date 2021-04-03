package com.svalero.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Country {

    private String name;
    private String capital;
    private String region;
    private String subregion;
    private String flag;
    private List<Bloc> regionalBlocs;
    private long population;

    @Override
    public String toString() {
        return name + " [" + population + "] ";
    }

    public String toCSV() { return name + ','  + capital + ',' + region + ',' + subregion + ',' + population + ',' + regionalBlocs; }
}
