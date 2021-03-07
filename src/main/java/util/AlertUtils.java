package util;

import javafx.scene.control.Alert;

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

    public static void showConfirm(String confirm){
        Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION);
        alertConfirm.setContentText(confirm);
        alertConfirm.show();
    }
}
