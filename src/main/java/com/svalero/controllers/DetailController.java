package com.svalero.controllers;

import com.svalero.domain.Country;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import util.R;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ResourceBundle;

public class DetailController implements Initializable {

    public WebView wvFlag = new WebView();
    public TableView<Country> tvDetailCountry;
    public ObservableList<Country> olCountryList;
    public ProgressIndicator piTableView;

    private Country country;

    public DetailController(Country country){
        this.country = country;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        olCountryList = FXCollections.observableArrayList();
        olCountryList.add(country);

        wvFlag.getEngine().load(country.getFlag());
        wvFlag.setZoom(0.4);

        setTable();
        loadTable();
    }

    public void loadTable(){
        tvDetailCountry.setItems(olCountryList);
    }

    public void setTable(){
        Field[] fields = Country.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("flag"))
                continue;
            TableColumn<Country, String> column = new TableColumn<>(field.getName());
            column.setCellValueFactory(new PropertyValueFactory<>(field.getName()));
            tvDetailCountry.getColumns().add(column);
        }
        tvDetailCountry.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    public void onBack(Event event){
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(R.getUi("countries.fxml"));
            loader.setController(new AppController());

            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

            Stage detailStage = (Stage) tvDetailCountry.getScene().getWindow();
            detailStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
