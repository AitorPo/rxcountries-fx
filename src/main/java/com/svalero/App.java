package com.svalero;

import com.svalero.service.CountriesController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.R;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(R.getUi("countries.fxml"));
        loader.setController(new CountriesController());
        VBox vBox = loader.load();

        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
