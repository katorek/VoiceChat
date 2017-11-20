package wjaronski.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import wjaronski.Main;
import wjaronski.socket.SocketConnection;
import wjaronski.voice.SoundMenager;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {
    private static Stage stage;
    private static boolean logoutRequest = false;
    private static SoundMenager sm;
    private MainWindowController controller;
    private static Mixer mixer;

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

    @FXML
    void settings(ActionEvent event) {
        loadSettingsWindow();
        sm.changeMic(mixer);
    }

    private void loadSettingsWindow() {
        mixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[3]);
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getResource("/settingsWindow.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Ustawienia dzwieku");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSettings(File file) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String m = br.readLine();
            System.err.println(m);
            Arrays.stream(SoundMenager.getMixers())
                    .filter(SoundMenager::isLineSupported)
                    .forEach(e -> {
                        System.err.println(e.toString());
                        mixer = (e.toString().equals(m)) ?
                                SoundMenager.getMixer(e) : mixer;
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(mixer.toString());
    }

    static void setMixer(Mixer mixer) {
        MainWindowController.mixer = mixer;
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

    private void updateListView() {
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
        mixer = null;
        initSettings();
        usersLogged = new ArrayList(10);
        SocketConnection socketConnection = Main.getSocketConnection();

        sm = new SoundMenager(socketConnection.getSocket(), mixer);
        sm.setController(this);
        sm.startPlaying();
        sm.startRecording();
    }

    private void initSettings() {
        File defaultSettings = new File(".soundSettings");
        if (defaultSettings.exists()) loadSettings(defaultSettings);
        else loadSettingsWindow();
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
