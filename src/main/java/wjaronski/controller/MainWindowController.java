package wjaronski.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import wjaronski.Main;
import wjaronski.socket.SocketConnection;
import wjaronski.voice.SoundMenager;

import java.net.URL;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable{
    private static Stage stage;
    private static boolean logoutRequest = false;
    private SocketConnection socketConnection;
    private static SoundMenager sm;

    @FXML
    private ToggleButton speakerToggleButton;

    @FXML
    private ToggleButton micToggleButton;

    @FXML
    private Button settingsButton;

    @FXML
    private Button logOutButton;

    @FXML
    void logout(ActionEvent event) {
        logoutRequest = true;
        stage = (Stage) logOutButton.getScene().getWindow();
        hide();
    }

    @FXML
    void settings(ActionEvent event) {
    }

    @FXML
    void micMute(ActionEvent event) {
        sm.setMicMuted(micToggleButton.isSelected());
    }

    @FXML
    void speakerMute(ActionEvent event) {
        sm.setSpeakerMuted(speakerToggleButton.isSelected());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        socketConnection = Main.getSocketConnection();
        sm = new SoundMenager(socketConnection.getSocket());
        sm.startPlaying();
        sm.startRecording();
    }

    public static void hide(){
        close();
        stage.hide();
    }

    public static void close(){
        sm.closeRequest();
        sm.cleanUp();
    }

    public static boolean isLogoutRequest() {
        return logoutRequest;
    }
}
