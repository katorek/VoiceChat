package wjaronski.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import wjaronski.voice.SoundMenager;

import javax.sound.sampled.Mixer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SettingsWindowController implements Initializable {
    private List<Mixer.Info> mixersInfo;
//    private Mixer selectedMixer;
    private Mixer.Info selectedMixerInfo;

    @FXML
    private ListView<Mixer.Info> mixersListView;

    @FXML
    private Button saveButton;

    @FXML
    private void saveSettings() {
        selectedMixerInfo = mixersListView.getSelectionModel().getSelectedItem();

        File file = new File(".soundSettings");
        if(file.exists() && file.delete()) System.out.println("Nie udalo sie zapisac do pliku ustawien dzwieku");
        else {
            try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                System.out.println(selectedMixerInfo.toString());
                bw.write(selectedMixerInfo.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        MainWindowController.setMixer(SoundMenager.getMixer(selectedMixerInfo));
        saveButton.getScene().getWindow().hide();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Mixer.Info[] mixers = SoundMenager.getMixers();
        mixersInfo = Arrays.stream(mixers)
                .filter(SoundMenager::isLineSupported)
                .collect(Collectors.toList());
        mixersListView.setItems(FXCollections.observableArrayList(mixersInfo));

    }
}
