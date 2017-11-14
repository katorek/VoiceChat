package wjaronski.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import wjaronski.Main;
import wjaronski.socket.SocketConnection;
import wjaronski.voice.SoundMenager;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {
    private static Stage stage;
    private static boolean logoutRequest = false;
    private static SoundMenager sm;
    private MainWindowController controller;
    private SocketConnection socketConnection;

    public MainWindowController getController() {
        return controller;
    }

    public void setController(MainWindowController controller) {
        this.controller = controller;
    }

    @FXML
    private ToggleButton speakerToggleButton;

    @FXML
    private ToggleButton micToggleButton;

    @FXML
    private Button settingsButton;

    @FXML
    private Button logOutButton;

    @FXML
    private ListView userListView;
    private ArrayList usersLogged;

    @FXML
    void logout(ActionEvent event) {
        logoutRequest = true;
        stage = (Stage) logOutButton.getScene().getWindow();
        hide();
    }

    @SuppressWarnings("unused")
    @FXML
    void settings(ActionEvent event) {
        //todo
    }

    @FXML
    void micMute(ActionEvent event) {
        sm.setMicMuted(micToggleButton.isSelected());
    }

    @FXML
    void speakerMute(ActionEvent event) {
        sm.setSpeakerMuted(speakerToggleButton.isSelected());
    }

    private void buildNewViewList() {
        usersLogged = new ArrayList(10);
    }

    private void addUserToList(String user) {
        usersLogged.add(user);
    }

    private void updateListView(){
        Platform.runLater(() -> userListView.setItems(FXCollections.observableArrayList(usersLogged)));
    }

    public void listViewByteArrayActions(byte[] arr) {
        switch (arr[99]) {
            case 55://'7'
                buildNewViewList();
                break;
            case 56://'8'
                addUserToList(usernameFromBuffer(arr));
                break;
            case 57://'9'
                updateListView();
                break;
        }
    }

    private String usernameFromBuffer(byte[] buffer) {
        StringBuilder sb = new StringBuilder("");
        for (byte b : buffer)
            sb.append((char) b);
        return sb.toString().substring(0, sb.length() - 2);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usersLogged = new ArrayList(10);
        socketConnection = Main.getSocketConnection();
        sm = new SoundMenager(socketConnection.getSocket());
        sm.setController(this);
        // todo otrzymanie listy uzytkownikow

        sm.startPlaying();
        sm.startRecording();
        requestLoggedUsers();
    }

    private void requestLoggedUsers() {
        socketConnection.requestLoggedUsers();
    }

    public static void hide() {
        close();
        stage.hide();
    }

    public static void close() {
        sm.closeRequest();
        sm.cleanUp();
    }

    public static boolean isLogoutRequest() {
        return logoutRequest;
    }
}
