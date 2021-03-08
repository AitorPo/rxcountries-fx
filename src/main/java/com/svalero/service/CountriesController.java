package com.svalero.service;

import com.svalero.domain.Country;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import javafx.scene.control.TextField;
import rx.Observable;
import rx.schedulers.Schedulers;


import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;

public class CountriesController implements Initializable {

    public TextField tfCountry;
    public ListView<Country> lvCountries;
    private CountriesService countriesService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        countriesService = new CountriesService();
        try{
            listCountries();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    public void listCountries() throws InterruptedException {
        lvCountries.getItems().clear();
        List<Country> list = null;


       list = countriesService.getAllCountries()
                .flatMap(Observable::from)
                .doAfterTerminate(() -> System.out.println("DONE"))
                .doOnCompleted(() -> System.out.println("Listado de países descargado"))
                .doOnError(throwable -> System.out.println(throwable.getMessage()))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .toList()
                .toBlocking()
                .first();
                      /*.flatMap(Observable::from)
                      .doOnCompleted(() -> System.out.println("Listado de países descargado"))
                      .doOnError(throwable -> System.out.println(throwable.getMessage()))
                      .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                      .subscribe(System.out::println);*/


        lvCountries.setItems(FXCollections.observableList(list));

    }
    @FXML
    public void findCountry(Event event) throws IOException {
        String name = tfCountry.getText();
        List<Country> list = null;


        list = countriesService.getCountry(name)
                .flatMap(Observable::from)
                .doOnCompleted(() -> System.out.println("Información de " + name + " descargada\n"))
                .doOnError(throwable -> System.out.println(throwable.getMessage()))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .toList()
                .toBlocking()
                .first();

        assert false;
        lvCountries.setItems(FXCollections.observableList(list));


        System.out.println("Comenzando descarga de información...");
    }
}
