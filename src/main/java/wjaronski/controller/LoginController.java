package wjaronski.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import wjaronski.socket.SocketConnection;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    private static String FILE_SEPARATOR = ";";
    private static String username, password, ip, port;

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
        //todo parse
        username = usernameTextField.getText();
        password = passwordField.getText();
        ip = ipTextField.getText();
        port = portTextField.getText();
        statusLabel.setText("u:" + username +
                ", p:" + password +
                ", ip:" + ip + ":" + port);
        SocketConnection sc = new SocketConnection(ip, port);
        sc.establishConnection();
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
        }
    }

    private void setUpdateListener() {
        ipTextField.textProperty().addListener((o,oldValue,newValue)-> ip = newValue);
        portTextField.textProperty().addListener((o,oldValue,newValue)-> port = newValue);
        usernameTextField.textProperty().addListener((o,oldValue,newValue)-> username = newValue);
        passwordField.textProperty().addListener((o,oldValue,newValue)-> password = newValue);
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
            e.printStackTrace();
        }
        if (!file.delete()) System.err.println("Couldnt delete settings file");
        ;
    }

    public static void close() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(".settings")))
        ) {
            StringBuilder sb = new StringBuilder();
            sb.append(ip).append(FILE_SEPARATOR)
                    .append(port).append(FILE_SEPARATOR)
                    .append(username).append(FILE_SEPARATOR)
                    .append(password);
            bw.write(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
