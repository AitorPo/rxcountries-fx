package util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class AlertUtils {

    public static void showError(String error){
        Alert alertError = new Alert(Alert.AlertType.ERROR);
        alertError.setContentText(error);
        alertError.show();
    }

    public static void showAlert(String info){
        Alert alertInfo = new Alert(Alert.AlertType.INFORMATION);
        alertInfo.setContentText(info);
        alertInfo.show();
    }

}
