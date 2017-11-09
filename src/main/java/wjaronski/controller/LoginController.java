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
import wjaronski.Main;
import wjaronski.exception.LogowanieNieudaneException;
import wjaronski.exception.LogowanieUdaneException;
import wjaronski.socket.SocketConnection;
import wjaronski.voice.SoundMenager;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    private static String NEW_USER_PREFIX = "5";
    private static String USER_LOGIN_PREFIX = "4";
    private static String FILE_SEPARATOR = ";";
    private static String username, password, ip, port;
    private static SocketConnection sc;
    private static boolean loggedProperly = false;

    private boolean waitingForLogResponse = false;

    private static SoundMenager soundMenager;

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
        updateFields();
        newSocketConnection();
        createResopnseThread(false);
        sendLoginDetails();

    }

    private void sendLoginDetails() {
        sc.send(USER_LOGIN_PREFIX + username + ";" + password + ";");
    }

    private void createResopnseThread(boolean newUser) {
        if (!waitingForLogResponse) {
            waitingForLogResponse = true;
            new Thread(() -> {
                if (!sc.loggedProperly()) {
                    try {
                        sc.loginResponse();
                    } catch (LogowanieNieudaneException e) {
                        logowanieNieudane(newUser);
                    } catch (LogowanieUdaneException e) {
                        logowanieUdane();
                    }
                }
            }).start();
            waitingForLogResponse = false;
        }
    }

    private void logowanieUdane() {
        loggedProperly = true;
        waitingForLogResponse = true;
        Platform.runLater(() -> {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.close();
        });
    }

    private void logowanieNieudane(boolean newUser) {
        closeSocket();
        loggedProperly = false;
        waitingForLogResponse = false;
        Platform.runLater(() -> {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText(newUser? "Użytkownik już istenije!":"Nieudane logowanie!");
        });
    }

    private void newSocketConnection() {
        sc = new SocketConnection(ip, port);
        Main.setSocketConnection(sc);
    }

    @FXML
    private void registerNewUser() {
        updateFields();
        newSocketConnection();
        createResopnseThread(true);
        sendNewUserDetails();

    }

    private void sendNewUserDetails() {
        sc.send(NEW_USER_PREFIX + username + ";" + password + ";");
    }

    private void updateFields() {
        username = usernameTextField.getText();
        password = passwordField.getText();
        ip = ipTextField.getText();
        port = portTextField.getText();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpdateListener();

        File defaultSettings = new File(".settings");
        if (defaultSettings.exists()) {
            loadSettings(defaultSettings);
        } else {
            try {
                ipTextField.setText(InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                ipTextField.setText("localhost");
            }
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

    public static SocketConnection getSc() {
        return sc;
    }

    private void closeSocket(){
        try {
            sc.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SoundMenager getSoundMenager() {
        return soundMenager;
    }

    public boolean loggedProperly() {
        return loggedProperly;
    }
}
