package com.svalero.service;

import com.svalero.domain.Country;
import com.svalero.domain.Region;
import javafx.collections.ObservableList;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import util.Constants;

import java.util.List;

public class CountriesService {

    private CountriesApiService api;

    public CountriesService(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        api = retrofit.create(CountriesApiService.class);
    }

    public Observable<List<Country>> getAllCountries(){
        return api.getAllCountries();
    }

    public Observable<List<Country>> getCountry(String name) { return api.getCountry(name); }


}
