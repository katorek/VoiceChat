package wjaronski.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import wjaronski.exception.LogowanieNieudaneException;
import wjaronski.exception.LogowanieUdaneException;
import wjaronski.socket.SocketConnection;
import wjaronski.voice.SoundMenager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    private static String FILE_SEPARATOR = ";";
    private static String username, password, ip, port;
    private SocketConnection sc;
    private boolean waitingForLogResponse = false;

    private SoundMenager soundMenager;


    @FXML
    private TextField ipTextField;

    @FXML
    private TextField portTextField;

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerBUtton;

    @FXML
    private Label statusLabel;

    @FXML
    private void logUser() {
        username = usernameTextField.getText();
        password = passwordField.getText();
        ip = ipTextField.getText();
        port = portTextField.getText();
//        statusLabel.setText("u:" + username +
//                ", p:" + password +
//                ", ip:" + ip + ":" + port);
        if (sc == null) {
            sc = new SocketConnection(ip, port);
        }
        System.out.println("Trying to send login");

        if (!waitingForLogResponse) {
            waitingForLogResponse = true;
            new Thread(() -> {
                if (!sc.loggedProperly()) {
                    try {
                        System.out.println("Waiting for response");
                        sc.loginResponse();
                    } catch (LogowanieNieudaneException e) {
                        waitingForLogResponse = false;
                        Platform.runLater(() -> {
                            statusLabel.setTextFill(Color.RED);
                            statusLabel.setText("Nieudane logowanie");
                        });
                    } catch (LogowanieUdaneException e) {
                        waitingForLogResponse = true;
                        Platform.runLater(() -> {
                            Stage stage = (Stage) loginButton.getScene().getWindow();
                            stage.close();
                        });
                        //otworzyc main window
                    }
                }

//                Platform.runLater(() -> statusLabel.setText(""));
            }).start();
        }
        sc.send(username + ";" + password + ";");
//        if (sc.loggedProperly()) {
//            System.out.println("SOUND STARTING");
//            soundMenager = new SoundMenager(sc.getSocket());
//            soundMenager.startPlaying();
//            soundMenager.startRecording();
//
//        }
    }

    @FXML
    private void registerNewUser() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpdateListener();

        File defaultSettings = new File(".settings");
        if (defaultSettings.exists()) {
            loadSettings(defaultSettings);
        } else {
            ipTextField.setText("localhost");
            portTextField.setText("12345");
        }
    }

    private void setUpdateListener() {
        ipTextField.textProperty().addListener((o, oldValue, newValue) -> ip = newValue);
        portTextField.textProperty().addListener((o, oldValue, newValue) -> port = newValue);
        usernameTextField.textProperty().addListener((o, oldValue, newValue) -> username = newValue);
        passwordField.textProperty().addListener((o, oldValue, newValue) -> password = newValue);
    }

    private void loadSettings(File file) {
        //todo oddzielone srednikiem dane
        try (BufferedReader br = new BufferedReader(new FileReader(file))
        ) {
            String[] arr = br.readLine().split(FILE_SEPARATOR);
            ipTextField.setText(ip = arr[0]);
            portTextField.setText(port = arr[1]);
            usernameTextField.setText(username = arr[2]);
            passwordField.setText(password = arr[3]);

        } catch (Exception e) {
            usernameTextField.setText(username = "");
            passwordField.setText(password = "");
        }
        if (!file.delete()) System.err.println("Couldnt delete settings file");
    }

    public static void close() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(".settings")))
        ) {
            StringBuilder sb = new StringBuilder();
            sb.append(ip).append(FILE_SEPARATOR)
                    .append(port).append(FILE_SEPARATOR)
                    .append(username == null ? "" : username).append(FILE_SEPARATOR)
                    .append(password == null ? "" : password);
            bw.write(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
