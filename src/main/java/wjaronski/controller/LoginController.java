package wjaronski.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import wjaronski.Main;
import wjaronski.socket.SocketConnection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import static wjaronski.socket.SocketConnection.LOGOWANIE_NIEUDANE;
import static wjaronski.socket.SocketConnection.LOGOWANIE_UDANE;
import static wjaronski.socket.SocketConnection.UZYTKOWNIK_JUZ_ISNIEJE;
import static wjaronski.socket.SocketConnection.UZYTKOWNIK_JUZ_ZALOGOWANY;
import static wjaronski.socket.SocketConnection.UZYTKOWNIK_NIE_ISNIEJE;


public class LoginController implements Initializable {
    private static final String FILE_SEPARATOR = ";";

    private static String username, password, ip, port;
    private static SocketConnection sc;
    private static boolean loggedProperly = false;

    private boolean waitingForLogResponse = false;


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

    @SuppressWarnings("unused")
    @FXML
    private Button registerbutton;

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
        String USER_LOGIN_PREFIX = "4";
        sc.send(USER_LOGIN_PREFIX + username + ";" + password + ";");
    }

    private void createResopnseThread(boolean newUser) {
        if (!waitingForLogResponse) {
            waitingForLogResponse = true;
            new Thread(() -> {
                if (!sc.loggedProperly()) {
                    int loginStatus = sc.loginResponse();
                    switch (loginStatus) {
                        case LOGOWANIE_UDANE: {
                            logowanieUdane();
                            break;
                        }
                        case LOGOWANIE_NIEUDANE: {
                            logowanieNieudane("Nie udane logowanie! Haslo nie poprawne!");
                            break;
                        }
                        case UZYTKOWNIK_JUZ_ZALOGOWANY: {
                            logowanieNieudane("Użytkownik jest już zalogowany!");
                            break;
                        }
                        case UZYTKOWNIK_JUZ_ISNIEJE: {
                            logowanieNieudane("Taki uzytkownik juz istnieje!");
                            break;
                        }
                        case UZYTKOWNIK_NIE_ISNIEJE: {
                            logowanieNieudane("Taki uzytownik nie istnieje!");
                            break;
                        }
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

    private void logowanieNieudane(String msg) {
        System.err.println("MSG: " + msg);
        closeSocket();
        loggedProperly = false;
        waitingForLogResponse = false;
        Platform.runLater(() -> {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText(msg);
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
        String NEW_USER_PREFIX = "5";
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
        setEnterListeners();

        loginButton.setText("Zaloguj \u21B5");

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

    private void setEnterListeners() {
        portTextField.setOnKeyPressed((e) -> {
            if (e.getCode().equals(KeyCode.ENTER)) logUser();
        });
        usernameTextField.setOnKeyPressed((e) -> {
            if (e.getCode().equals(KeyCode.ENTER)) logUser();
        });
        passwordField.setOnKeyPressed((e) -> {
            if (e.getCode().equals(KeyCode.ENTER)) logUser();
        });
        ipTextField.setOnKeyPressed((e) -> {
            if (e.getCode().equals(KeyCode.ENTER)) logUser();
        });
    }

    private void setUpdateListener() {
        ipTextField.textProperty().addListener((o, oldValue, newValue) -> ip = newValue);
        portTextField.textProperty().addListener((o, oldValue, newValue) -> port = newValue);
        usernameTextField.textProperty().addListener((o, oldValue, newValue) -> username = newValue);
        passwordField.textProperty().addListener((o, oldValue, newValue) -> password = newValue);
    }

    private void loadSettings(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))
        ) {
            String[] arr = br.readLine().split(FILE_SEPARATOR);
            ipTextField.setText(ip = arr[0]);
            portTextField.setText(port = arr[1]);
            usernameTextField.setText(username = (arr[2] == null) ? "" : arr[2]);
            passwordField.setText(password = (arr[3] == null) ? "" : arr[3]);
        } catch (Exception e) {
            usernameTextField.setText(username = "");
            passwordField.setText(password = "");
        }
        if (!file.delete()) System.err.println("Couldnt delete settings file");
    }

    public static void close() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(".settings")))
        ) {
            String sb = ip + FILE_SEPARATOR +
                    port + FILE_SEPARATOR +
                    (username == null ? "" : username) + FILE_SEPARATOR +
                    (password == null ? "" : password);
            bw.write(sb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SocketConnection getSc() {
        return sc;
    }

    private void closeSocket() {
        try {
            sc.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean loggedProperly() {
        return loggedProperly;
    }
}
