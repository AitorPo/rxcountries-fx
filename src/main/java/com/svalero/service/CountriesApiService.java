package com.svalero.service;

import com.svalero.domain.Country;
import javafx.collections.ObservableList;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

import java.util.List;

public interface CountriesApiService {

    @GET("/rest/v2/all")
    Observable<List<Country>> getAllCountries();

    @GET("/rest/v2/name/{name}")
    Observable<List<Country>> getCountry(@Path("name") String name);

}
