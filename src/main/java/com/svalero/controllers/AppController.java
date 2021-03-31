package com.svalero.controllers;

import com.svalero.domain.Country;
import com.svalero.service.CountriesService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;

import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

import javafx.stage.Stage;
import rx.Observable;
import rx.schedulers.Schedulers;
import util.R;


import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class AppController implements Initializable {

    public TextField tfCountry;
    public ListView<Country> lvCountries;
    public ListView<Country> lvCountriesByRegion;
    public ObservableList<Country> countryByRegion;
    public ObservableList<Country> list;
    public ProgressIndicator piListViewCountriesByName;
    public ProgressIndicator piListViewCountriesByRegion;


    private CountriesService countriesService;
    private Country selectedCountry;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        countriesService = new CountriesService();
        list = FXCollections.observableArrayList();
        countryByRegion = FXCollections.observableArrayList();
        piListViewCountriesByName.setVisible(false);
        piListViewCountriesByRegion.setVisible(false);

        listCountries();

    }

    @FXML
    public void listCountries() {
       lvCountries.getItems().clear();
       piListViewCountriesByName.setVisible(true);
       piListViewCountriesByName.setProgress(-1);
       lvCountries.setItems(list);

       countriesService.getAllCountries()
               .flatMap(Observable::from)
               .doOnCompleted(() -> {
                   System.out.println("Países cargados");
                   piListViewCountriesByName.setVisible(false);
               })
               .doOnError(throwable -> System.out.println(throwable.getMessage()))
               .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
               .subscribe(country -> {
                   Platform.runLater(() -> {
                       list.add(country);
                       System.out.println(country.getName() + " añadido a la lista");
                   });

               });
        tfCountry.clear();
    }

    @FXML
    public void findCountryByName(Event event) {
        lvCountries.getItems().clear();
        piListViewCountriesByName.setVisible(true);
        piListViewCountriesByName.setProgress(-1);
        lvCountries.setItems(list);
        String name = tfCountry.getText();

        countriesService.getCountry(name)
                .flatMap(Observable::from)
                .doOnCompleted(() -> {
                    piListViewCountriesByName.setVisible(false);
                })
                .doOnError(throwable -> System.out.println(throwable.getMessage()))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(country ->
                        Platform.runLater(()-> list.add(country)));;

        System.out.println("Comenzando descarga de información...");
        tfCountry.clear();
    }

    @FXML
    public void findCountryByRegion(Event event){
        List<Country> lista = new ArrayList<>(list);
        String region = tfCountry.getText();

        lvCountriesByRegion.getItems().clear();
        piListViewCountriesByRegion.setVisible(true);
        lvCountriesByRegion.setItems(countryByRegion);

        Observable<Country> countryObservable = Observable.from(lista);
        countryObservable.filter(country -> country.getRegion().equals(region))
                .doOnCompleted(() -> {
            System.out.println("Información de " + region + " descargada\n");
            piListViewCountriesByRegion.setVisible(false);
                })
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
            .subscribe(country -> {
               countryByRegion.add(country);

        });



        /*countryObservable.doOnCompleted(() -> {
            System.out.println("Información de " + region +" descargada\n");
            piListViewCountriesByRegion.setVisible(false);
        })
        .doOnError(throwable -> System.out.println(throwable.getMessage()))
        .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
        .subscribe(countries -> {
            countryByRegion.addAll(countries);
            countryByRegion.stream().filter()
        });*/



    }

    @FXML
    public void detailIntent(Event event){
        try {
            selectedCountry = lvCountries.getSelectionModel().getSelectedItem();
            Stage stage = new Stage();
            DetailController detailController = new DetailController(selectedCountry);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(R.getUi("detail_country.fxml"));
            loader.setController(detailController);
            //VBox vBox = null;
            //vBox = loader.load();
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

            Stage listStage = (Stage) tfCountry.getScene().getWindow();
            listStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
