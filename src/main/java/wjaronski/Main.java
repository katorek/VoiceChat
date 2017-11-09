package wjaronski;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import wjaronski.controller.LoginController;
import wjaronski.controller.MainWindowController;
import wjaronski.socket.SocketConnection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main extends Application {
    private static SocketConnection socketConnection;
    private Stage loginStage, mainStage;
    private boolean stageClosing = false;
    private boolean mainStageClosed = false;

    @Override
    public void start(Stage stage) throws Exception {
        this.loginStage = stage;
        loadLoginWindow(stage);
    }

    private void loadLoginWindow(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/loginWindow.fxml"));
            stage.setTitle("Komunikator glosowy");
            stage.setScene(new Scene(root));
            stage.setOnCloseRequest(e -> {
                LoginController.close();
                stageClosing = true;
            });
            stage.setOnHiding(e -> {
                if (!stageClosing)
                    loadMainWindow();
            });
            socketConnection = LoginController.getSc();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMainWindow() {
        LoginController.close();
        loginStage.close();
        mainStageClosed = false;
        try {
            Parent mainWindow = FXMLLoader.load(getClass().getResource("/mainWindow.fxml"));
            mainStage = new Stage();
            mainStage.initModality(Modality.WINDOW_MODAL);
            mainStage.setTitle("Komunikator glosowy");
            mainStage.setScene(new Scene(mainWindow));
            mainStage.setOnCloseRequest(e -> {
                mainStageClosed = true;
                MainWindowController.close();
            });
            mainStage.setOnHiding(e -> {
                if (!mainStageClosed) {
                    MainWindowController.hide();
                    if (MainWindowController.isLogoutRequest()) loadLoginWindow(new Stage());
                }
            });
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static SocketConnection getSocketConnection() {
        return socketConnection;
    }

    public static void setSocketConnection(SocketConnection socketConnection) {
        Main.socketConnection = socketConnection;
    }
}
