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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import rx.Observable;
import rx.schedulers.Schedulers;
import util.AlertUtils;
import util.Constants;
import util.R;


import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static util.Constants.FLUX_URI;

public class AppController implements Initializable {

    public TextField tfCountry;
    public ListView<Country> lvCountries;
    public ComboBox<String> cbRegions;
    public ComboBox<String> cbBloc;
    public Label lblSelectedCountry;
    public ProgressIndicator piListViewCountriesByName;

    private WebClient webClient;

    private ObservableList<Country> list;
    private ObservableList<String> regionObservableList;
    private ObservableList<String> blocsObservableList;
    private CountriesService countriesService;
    private Country selectedCountry;
    private String selectedRegion;
    private String selectedBloc;
    private File file;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        countriesService = new CountriesService();
        webClient = WebClient.create(Constants.URL);
        list = FXCollections.observableArrayList();
        regionObservableList = FXCollections.observableArrayList();
        blocsObservableList = FXCollections.observableArrayList();
        piListViewCountriesByName.setVisible(false);

        listCountries();
        loadRegionsComboBox();
        loadBlocsComboBox();
    }

    private void loadRegionsComboBox(){
        cbRegions.setItems(regionObservableList);
        countriesService.getAllCountries()
                .flatMap(Observable::from)
                .distinct(Country::getRegion)
                .doOnCompleted(() -> {
                    System.out.println("Regiones cargadas");
                })
                .doOnError(throwable -> System.out.println(throwable.getMessage()))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(country -> {
                    Platform.runLater(() -> {
                        // En la API de RESTCountries hay una region en blanco con dos pa??ses vac??os.
                        // Con este if evitamos que el continente en blanco de la API haga que aparezca un selectable
                        // del ComboBox en blanco
                        if (country.getRegion().equals("")
                                || country.getRegion().isEmpty()
                                || country.getRegion() == null){
                            country.setRegion("Pandora");}
                        regionObservableList.add(country.getRegion());
                        System.out.println(country.getRegion() + " a??adido a la lista");
                    });
                });

    }

    private void loadBlocsComboBox(){
        cbBloc.setItems(blocsObservableList);

        countriesService.getAllCountries()
                .flatMap(Observable::from)
                .distinct(Country::getRegionalBlocs)
                .doOnCompleted(() -> {
                    System.out.println("Bloques regionales cargados");
                })
                .doOnError(throwable -> System.out.println(throwable.getMessage()))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(country -> {
                    Platform.runLater(() -> {
                        // Comprobamos que la lista no sea null
                        if (country.getRegionalBlocs() != null ) {
                            // Como en la API existen continentes y pa??ses vac??os tenemos que comprobar que la lista de
                            // bloques regionales no est?? vac??a para que no salte un error IndexOutOfBoundsException
                            // y cuando nos topemos con dichos pa??ses los omitir?? a la hora de a??adir su bloque regional
                            // puesto que no tienen ninguno y as?? evitamos el error mencionado anteriormente
                            if (country.getRegionalBlocs().isEmpty()) return;
                            blocsObservableList.add(country.getRegionalBlocs().get(0).toString());
                                if (country.getRegionalBlocs().size() > 1){
                                    blocsObservableList.add(country.getRegionalBlocs().get(1).toString());
                                }
                                if (country.getRegionalBlocs().size() > 2){
                                    blocsObservableList.add(country.getRegionalBlocs().get(2).toString());
                                }
                        }
                    });
                });
    }

    @FXML
    public void listCountries() {
       // Limpiamos la lista para poder rellenarla cada vez que pulsamos un bot??n que desencadena un event
       // As?? nos evitamos instanciar varias listas
       list.clear();
       lvCountries.getItems().clear();
       piListViewCountriesByName.setVisible(true);
       piListViewCountriesByName.setProgress(-1);
       lvCountries.setItems(list);

       countriesService.getAllCountries()
               .flatMap(Observable::from)
               .doOnCompleted(() -> {
                   System.out.println("Pa??ses cargados");
                   piListViewCountriesByName.setVisible(false);
               })
               .doOnError(throwable -> System.out.println(throwable.getMessage()))
               .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
               .subscribe(country -> {
                   Platform.runLater(() -> {
                       list.add(country);
                       System.out.println(country.getName() + " a??adido a la lista");
                   });

               });
        tfCountry.clear();
    }

    @FXML
    public void findCountryByName(Event event) {
        // Limpiamos la lista para poder rellenarla cada vez que pulsamos un bot??n que desencadena un event
        // As?? nos evitamos instanciar varias listas
        list.clear();
        lvCountries.getItems().clear();
        piListViewCountriesByName.setVisible(true);
        piListViewCountriesByName.setProgress(-1);
        lvCountries.setItems(list);
        String name = tfCountry.getText().toLowerCase();
        if (name.isEmpty()){
            AlertUtils.showError("Debes escribir el nombre de un pa??s para poder realizar su b??squeda");
            listCountries();
            tfCountry.requestFocus();
            return;
        }

        countriesService.getCountry(name)
                .flatMap(Observable::from)
                .doOnCompleted(() -> {
                    piListViewCountriesByName.setVisible(false);
                })
                .doOnError(throwable -> System.out.println(throwable.getMessage()))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(country ->
                        Platform.runLater(()-> list.add(country)));

        System.out.println("Comenzando descarga de informaci??n...");
        tfCountry.clear();
    }

    @FXML
    public void findCountryByBloc(Event event){
        list.clear();
        lvCountries.getItems().clear();
        piListViewCountriesByName.setVisible(true);
        piListViewCountriesByName.setProgress(-1);
        lvCountries.setItems(list);
        selectedBloc = cbBloc.getSelectionModel().getSelectedItem();
        if (selectedBloc == null){
            AlertUtils.showError("Debes seleccionar un bloque regional en el ComboBox para poder consumir la API con WebFlux");
            listCountries();
            cbBloc.requestFocus();
            return;
        }

        Flux<Country> countriesFlux = webClient.get()
                .uri(FLUX_URI + selectedBloc)
                .retrieve()
                .bodyToFlux(Country.class);

        countriesFlux.doOnError(System.out::println)
                .doOnComplete(() -> {
                    System.out.println("Pa??ses por bloque regonal cargados");
                    piListViewCountriesByName.setVisible(false);
                })
                .subscribe(country -> {
                    Platform.runLater(() -> {
                        list.add(country);
                    });
                });
    }

    @FXML
    public void findCountryByRegion(Event event){
        // Limpiamos la lista para poder rellenarla cada vez que pulsamos un bot??n que desencadena un event
        // As?? nos evitamos instanciar varias listas
        list.clear();
        lvCountries.getItems().clear();
        piListViewCountriesByName.setVisible(true);
        piListViewCountriesByName.setProgress(-1);
        selectedRegion = cbRegions.getSelectionModel().getSelectedItem();
        if (selectedRegion == null){
            AlertUtils.showError("Debes seleccionar una regi??n en el ComboBox para poder realizar el filtrado");
            listCountries();
            cbRegions.requestFocus();
            return;
            // Como hemos asignado un nombre a la regi??n el blanco de la API
            // Tenemos que pasar el valor de ese selectable como "" para que devuelva los pa??ses vac??os de esa regi??n
            // Con este if gestionamos la selecci??n de dicha regi??n
        } else if (selectedRegion.equals("Pandora")) selectedRegion = "";

        lvCountries.setItems(list);
        String finalSelectedRegion = selectedRegion;

        // Obtenemos la lista total de los pa??ses de la API
        countriesService.getAllCountries()
                // "Separamos" la lista en objetos de la clase Country para acceder a sus propiedades y poder filtrar
                .flatMap(Observable::from)
                // A partir de la lista de la API filtramos los pa??ses por "region" (continente)
                .filter(country -> country.getRegion().equals(finalSelectedRegion))
                // Acci??n a realizar cuando se complete el Observable
                .doOnCompleted(() -> piListViewCountriesByName.setVisible(false))
                // Acci??n a realizar si falla el Observable
                .doOnError(throwable -> System.out.println(throwable.getMessage()))
                // Creaci??n de ThreadPool
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                // Suscripci??n al objeto de la clase Country para a??adirlo a un observableArrayList y poder setear la ListView
                .subscribe(country -> {
                    Platform.runLater(() -> {
                        list.add(country);
                        System.out.println(country.getName() + " a??adido a la lista de la regi??n de " + country.getRegion());
                    });
                });
    }

    @FXML
    public void setName(Event event){
        selectedCountry = lvCountries.getSelectionModel().getSelectedItem();
        String country = selectedCountry.getName();
        lblSelectedCountry.setText(country);
    }

    @FXML
    public void detailIntent(Event event){
        try {
            selectedCountry = lvCountries.getSelectionModel().getSelectedItem();
            if (selectedCountry == null){
                AlertUtils.showError("Debes seleccionar un pa??s de la lista para poder ver sus detalles");
                lvCountries.requestFocus();
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
        conf.setContentText("??Deseas exportar los datos en un archivo .csv?");
        Optional<ButtonType> res = conf.showAndWait();
        if (res.get().getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) return;
            if (createAndSaveCSV() != null ){
                AlertUtils.showAlert("Datos exportados a CSV correctamente");
            }
    }

    @FXML
    public void onZip(Event event) throws ExecutionException, InterruptedException {
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setTitle("Exportar a ZIP");
        conf.setContentText("??Deseas exportar los datos en un archivo .zip?");
        Optional<ButtonType> res = conf.showAndWait();
        if (res.get().getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) return;

        file = createAndSaveCSV();

        CompletableFuture.supplyAsync(() -> file.getAbsolutePath().concat(".zip"))
                .thenAccept(System.out::println)
                .whenComplete((unused, throwable) -> {
                  System.out.println("Zip generado en " + file.getAbsolutePath().concat(".zip"));
                      Platform.runLater(() -> {
                          exportZippedFile(file);
                          AlertUtils.showAlert("Zip generado correctamente");
                      });
                }).get();
    }

    private void exportZippedFile(File file){
        try{
            FileOutputStream fos = new FileOutputStream(file.getAbsolutePath().concat(".zip"));
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

            // Borramos el archivo que se genera al invocar al m??todo createAndSaveCSV()
            // para evitar duplicidades y que solo se genere el .zip del mismo
            Files.delete(Path.of(file.getAbsolutePath() + ".csv"));
        } catch (Exception e){
            e.printStackTrace();
            AlertUtils.showError("Error al zippear");
        }
    }

    private File createAndSaveCSV(){
        try{
            FileChooser fileChooser = new FileChooser();
            file = fileChooser.showSaveDialog(tfCountry.getScene().getWindow());
            FileWriter fileWriter = new FileWriter(file + ".csv");

            CSVPrinter csvPrinter = new CSVPrinter(fileWriter,
                CSVFormat.DEFAULT.withHeader("Pa??s", "Capital", "Continente", "Subregi??n", "Poblaci??n"));

            List<Country> countryList = new ArrayList<>(list);

            for (Country country : countryList)
                csvPrinter.printRecords(country.toCSV().replace(',', ';'));

            csvPrinter.close();
        } catch (IOException ioe){
            ioe.printStackTrace();
            AlertUtils.showError("Error al exportar los datos");
        }
        modifyCSV(file.getAbsolutePath().concat(".csv"), ",", ";");
        return file;
    }

    /**
     * M??todo con el que gestionaremos los separadores de nuestro csv para que Excel, por ejemplo,
     * interprete y coloque toda la informaci??n en columnas de forma odenada
     *
     * @param path = archivo a modificar
     * @param oldChar = delimiter (separador) que queremos cambiar
     * @param newChar = delimiter (separador) que queremos usar
     */
    private void modifyCSV(String path, String oldChar, String newChar){
        File fileToModify = new File(path);
        String oldContent = "";
        BufferedReader bf = null;
        FileWriter fw = null;
        try{
            bf = new BufferedReader(new FileReader(fileToModify));
            // Leemos todas las l??neas del archivo a modificar
            String line = bf.readLine();
            // Recorremos dichas l??neas
            while (line != null){
                // Almacenamos cada l??nea dentro de "oldContent"
                oldContent = oldContent + line + System.lineSeparator();
                // Este while acabar?? cuando no queden m??s l??neas que leer
                line = bf.readLine();
            }
            // Dentro de newContent almacenaremos el contenido ya actualizado
            String newContent = oldContent.replaceAll(",", ";");

            // Reescribimos el fichero
            fw = new FileWriter(fileToModify);
            fw.write(newContent);

        } catch (IOException ioe){
            ioe.printStackTrace();
            AlertUtils.showError("Error");
        } finally {
            try {
                bf.close();
                fw.close();
            } catch (IOException ioe){
                ioe.printStackTrace();
            }
        }
    }
}
