package wjaronski;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import wjaronski.controller.LoginController;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        loadLoginWindow(stage);
        loadMainWindow(stage);
    }

    private void loadLoginWindow(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/loginWindow.fxml"));
        stage.setTitle("Komunikator glosowy");
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(e -> LoginController.close());
        stage.show();
    }

    private void loadMainWindow(Stage stage) {

    }


    public static void main(String[] args) {
        launch(args);
    }
}
