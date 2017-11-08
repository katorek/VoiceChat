package wjaronski.voice;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class SoundMenager {
    private static final Integer BUFF_SIZE = 1000;

    private boolean isRecording = false;
    private boolean isPlaying = false;
    private ByteArrayOutputStream byteArrayOutputStream;
    private AudioFormat audioFormat;
    private TargetDataLine targetDataLine;
    private AudioInputStream audioInputStream;
    private BufferedOutputStream out = null;
    private BufferedInputStream in = null;
    private Socket sock = null;
    private SourceDataLine sourceDataLine;
    private Mixer.Info[] mixers;


    public SoundMenager(Socket socket, Mixer mixer) {
        setUpSocket(socket);
        setUpMic(mixer);
        setUpOutput();
    }

    private void setUpSocket(Socket socket) {
        try {
            this.sock = socket;
            out = new BufferedOutputStream(socket.getOutputStream());
            in = new BufferedInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpMic(Mixer mixer) {
        try {
            audioFormat = getAudioFormat();

            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

            targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
            targetDataLine.open(audioFormat);
            targetDataLine.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void setUpOutput() {
        try {
            DataLine.Info dataLineInfo = new DataLine.Info(
                    SourceDataLine.class, audioFormat);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        int sampleSizeInBits = 8;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;

        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed,
                bigEndian);
    }

    public void startRecording() {
        byte buffer[] = new byte[BUFF_SIZE];
        isRecording = true;
        new Thread(() -> {
            try {
                while (isRecording()) {
                    targetDataLine.read(buffer, 0, BUFF_SIZE);
                    out.write(buffer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stopRecording() {
        isRecording = false;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void startPlaying() {
        byte buffer[] = new byte[BUFF_SIZE];
        isPlaying = true;
        new Thread(() -> {
            try {
                while (isPlaying() && in.read(buffer) != -1) {
                    sourceDataLine.write(buffer, 0, BUFF_SIZE);
                }
                sourceDataLine.drain();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean isPlaying() {
        return isPlaying;
    }

    public void stopPlaying() {
        isPlaying = false;
    }

    public void cleanUp() {
        isPlaying = false;
        isRecording = false;
        sourceDataLine.close();
    }

    /**
     * Aby dostac mixer wspierajace wysylanie dzwieku to wykonac
     * wywolac isLineSupported(mixer) na SoundManagerze
     * @return list of Mixers
     */
    public Mixer.Info[] getMixers(){
        audioFormat = getAudioFormat();

        DataLine.Info dataLineInfo = new DataLine.Info(
                TargetDataLine.class, audioFormat);

        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        return AudioSystem.getMixerInfo();
    }

    public boolean isLineSupported(Mixer mixer){
        DataLine.Info dataLineInfo = new DataLine.Info(
                TargetDataLine.class, audioFormat);
        return mixer.isLineSupported(dataLineInfo);
    }

    public static void main(String[] args) {

        try {
            Socket socket = new Socket("192.168.1.18", 12345);
            Mixer mixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[3]);

            SoundMenager sm = new SoundMenager(socket,mixer);
            sm.startRecording();
            sm.startPlaying();

            Scanner sc = new Scanner(System.in);

            while (!sc.nextLine().equals("0")) ;
            System.out.println("Ending");
            sm.cleanUp();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
