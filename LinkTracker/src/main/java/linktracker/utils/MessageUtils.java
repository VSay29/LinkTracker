package linktracker.utils;

import javafx.scene.control.Alert;

public class MessageUtils {

    /**
     * Función para mostrar mensajes de error
     * @param message: Mensaje que se mostrará
     */

    public static void showError(String message, String encabezado){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(encabezado);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Función para mostrar mensajes de información
     * @param message: Mensaje que se mostrará
     */

    public static void showMessage(String message, String encabezado){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mensaje");
        alert.setHeaderText(encabezado);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
