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
import javafx.scene.control.*;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import rx.Observable;
import rx.schedulers.Schedulers;
import util.AlertUtils;
import util.R;


import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AppController implements Initializable {

    public TextField tfCountry;
    public ListView<Country> lvCountries;
    public ObservableList<Country> list;
    public ObservableList<String> regionObservableList;
    public ProgressIndicator piListViewCountriesByName;
    public ComboBox<String> cbRegions;

    private CountriesService countriesService;
    private Country selectedCountry;
    private String selectedRegion;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        countriesService = new CountriesService();
        list = FXCollections.observableArrayList();
        regionObservableList = FXCollections.observableArrayList();
        piListViewCountriesByName.setVisible(false);

        listCountries();
        loadComboBox();

    }

    private void loadComboBox(){
        cbRegions.setItems(regionObservableList);
        countriesService.getAllCountries()
                .flatMap(Observable::from)
                .distinct(Country::getRegion)
                .doOnCompleted(() -> {
                    System.out.println("Regiones cargadas");
                    piListViewCountriesByName.setVisible(false);
                })
                .doOnError(throwable -> System.out.println(throwable.getMessage()))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(country -> {
                    Platform.runLater(() -> {
                        // En la API de RESTCountries hay una region en blanco con dos países vacíos.
                        // Con este if evitamos que el continente en blanco de la API haga que aparezca un selectable
                        // del ComboBox en blanco
                        if (country.getRegion().equals("")
                                || country.getRegion().isEmpty()
                                || country.getRegion() == null){
                            country.setRegion("Continente en blanco");}
                        regionObservableList.add(country.getRegion());
                        System.out.println(country.getRegion() + " añadido a la lista");
                    });
                });

    }

    @FXML
    public void listCountries() {
        list.clear();
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
        list.clear();
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
                        Platform.runLater(()-> list.add(country)));

        System.out.println("Comenzando descarga de información...");
        tfCountry.clear();
    }

    @FXML
    public void findCountryByRegion(Event event){
        list.clear();
        lvCountries.getItems().clear();
        piListViewCountriesByName.setVisible(true);
        piListViewCountriesByName.setProgress(-1);
        selectedRegion = cbRegions.getSelectionModel().getSelectedItem();
        // Como hemos asignado un nombre a la región el blanco de la API
        // Tenemos que pasar el valor de ese selectable como "" para que devuelva los países vacíos de esa región
        // Con este if gestionamos la selección de dicha región
        if (selectedRegion.equals("Continente en blanco")) selectedRegion = "";
        lvCountries.setItems(list);

        String finalSelectedRegion = selectedRegion;
        // Obtenemos la lista total de los países de la API
        countriesService.getAllCountries()
                // "Separamos" la lista en objetos de la clase Country para acceder a sus propiedades y poder filtrar
                .flatMap(Observable::from)
                // A partir de la lista de la API filtramos los países por "region" (continente)
                .filter(country -> country.getRegion().equals(finalSelectedRegion))
                // Acción a realizar cuando se complete el Observable
                .doOnCompleted(() -> piListViewCountriesByName.setVisible(false))
                // Acción a realizar si falla el Observable
                .doOnError(throwable -> System.out.println(throwable.getMessage()))
                // Creación de ThreadPool
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                // Suscripción al objeto de la clase Country para añadirlo a un observableArrayList y poder setear la ListView
                .subscribe(country -> {
                    Platform.runLater(() -> {
                        list.add(country);
                        System.out.println(country.getName() + " añadido a la lista de la región de " + country.getRegion());
                    });
                });
    }

    @FXML
    public void detailIntent(Event event){
        try {
            selectedCountry = lvCountries.getSelectionModel().getSelectedItem();
            if (selectedCountry == null){
                AlertUtils.showError("Selecciona un país de la lista");
                return;
            }

            Stage stage = new Stage();
            DetailController detailController = new DetailController(selectedCountry);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(R.getUi("detail_country.fxml"));
            loader.setController(detailController);

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

    @FXML
    public void onExport(Event event){
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setTitle("Exportar a CSV");
        conf.setContentText("¿Deseas exportar los datos en un archivo .csv?");
        Optional<ButtonType> res = conf.showAndWait();
        if (res.get().getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) return;
            if (createAndSaveCSV() != null ){
                AlertUtils.showAlert("Datos exportados a CSV correctamente");
            }
    }

    @FXML
    public void onZip(Event event){
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setTitle("Exportar a ZIP");
        conf.setContentText("¿Deseas exportar los datos en un archivo .zip?");
        Optional<ButtonType> res = conf.showAndWait();
        if (res.get().getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) return;

        File file = createAndSaveCSV();
        exportZippedFile(file);
    }

    private void exportZippedFile(File file){
        try{
            FileOutputStream fos = new FileOutputStream("C:\\Users\\User\\Desktop\\"+ file.getName().concat(".zip"));
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            //File fileToZip = new File("C:\\Users\\User\\Desktop\\" + source);
            FileInputStream fis = new FileInputStream(file + ".csv");
            ZipEntry zipEntry = new ZipEntry(file.getName().concat(".csv"));
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0){
                zipOut.write(bytes, 0, length);
            }
            zipOut.close();
            fis.close();
            fos.close();

            // Borramos el archivo que se genera al invocar al método createAndSaveCSV()
            // para evitar duplicidades y que solo se genere el .zip del mismo
            Files.delete(Path.of(file.getAbsolutePath() + ".csv"));
            AlertUtils.showAlert("ZIP generado correctamente");

        } catch (Exception e){
            e.printStackTrace();
            AlertUtils.showError("Error al zippear");
        }

    }

    private File createAndSaveCSV(){
        File file = null;
        try{
            FileChooser fileChooser = new FileChooser();
            file = fileChooser.showSaveDialog(tfCountry.getScene().getWindow());
            FileWriter fileWriter = new FileWriter(file + ".csv");

            CSVPrinter csvPrinter = new CSVPrinter(fileWriter,
                    CSVFormat.DEFAULT.withHeader("País", "Capital", "Continente", "Subregión", "Población"));

            List<Country> countryList = new ArrayList<>(list);

            for (Country country : countryList)
                csvPrinter.printRecords(country.toCSV().replace(',', ';'));

            csvPrinter.close();

        } catch (IOException ioe){
            ioe.printStackTrace();
            AlertUtils.showError("Error al exportar los datos");
        }
        return file;
    }




   }
