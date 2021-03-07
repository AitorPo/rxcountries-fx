package com.svalero.service;

import com.svalero.domain.Country;
import com.svalero.service.CountriesService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import javafx.scene.control.TextField;
import retrofit2.Call;
import rx.Observable;
import rx.schedulers.Schedulers;
import util.AlertUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
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
    public void listCountries() throws IOException {
        lvCountries.getItems().clear();
        Call<List<Country>> countries = null;
        try {
             countries = countriesService.getAllCountries();
        } catch (Exception e) {
            AlertUtils.showError("Error al cargar los datos de la aplicación");
            e.printStackTrace();
        }
        assert countries != null;
        lvCountries.setItems(FXCollections.observableList(Objects.requireNonNull(countries.execute().body())));
    }
    @FXML
    public void findCountry(Event event) throws IOException {
        String name = tfCountry.getText();
        lvCountries.getItems().clear();
        Call<List<Country>> country = null;
        try{
            country = countriesService.getCountry(name);
        }catch (Exception e){
            AlertUtils.showError("Error al cargar los datos de la aplicación");
            e.printStackTrace();
        }
        assert country != null;
        lvCountries.setItems(FXCollections.observableList(Objects.requireNonNull(country.execute().body())));
    }
}
