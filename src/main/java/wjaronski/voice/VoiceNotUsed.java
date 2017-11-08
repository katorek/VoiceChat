package wjaronski.voice;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class VoiceNotUsed {
    private static final Integer CHUNK_SIZE = 1024;
    private final Lock mutex = new ReentrantLock(true);

    private boolean running;
    private AudioFormat format;
    private TargetDataLine microphone;
    private SourceDataLine speakers;
//    private ByteArrayOutputStream out;

    private byte[] dataFromMic;
    private byte[] dataToPlay;
    private Integer bytesFromMic = 0;
    private Integer bytesToPlay = 0;

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

    public void setDataToPlay(byte[] dataToPlay) {
        this.dataToPlay = dataToPlay;
    }

    public void setBytesToPlay(Integer bytesToPlay) {
        this.bytesToPlay = bytesToPlay;
    }

    public byte[] getDataFromMic() {
        return dataFromMic;
    }

    public Integer getBytesFromMic() {
        return bytesFromMic;
    }

    private void init() {
        // https://stackoverflow.com/questions/25798200/java-record-mic-to-byte-array-and-play-sound
        format = new AudioFormat(8000.0f, 16, 1, true, true);
        try {
            microphone = AudioSystem.getTargetDataLine(format);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);

//            out = new ByteArrayOutputStream();
            dataFromMic = new byte[microphone.getBufferSize() / 5];
            dataToPlay = new byte[microphone.getBufferSize() / 5];
            microphone.start();

            speakers = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, format));
            speakers.open(format);
            speakers.start();
            running = true;
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        new Thread(() -> {
            while (isRunning()) {
                mutex.lock();
                bytesFromMic = microphone.read(dataFromMic, 0, CHUNK_SIZE);
                bytesToPlay = bytesFromMic;
                dataToPlay = dataFromMic;
                mutex.unlock();
            }
        }).start();
    }

    public void play() {
        new Thread(() -> {
            while (isRunning()) {
                mutex.lock();
//                dataOut = dataToPlay;
//                out.write(dataToPlay, 0, bytesToPlay);
                speakers.write(dataToPlay, 0, bytesToPlay);
                mutex.unlock();
            }
        }).start();
    }

    public void cleanUp() {
        running = false;
//        while (running) ;
//        System.err.println("ENDING");
        speakers.drain();
        speakers.close();
        microphone.close();
    }

    public VoiceNotUsed(){
        init();
    }

    public static void main(String[] args) {
        VoiceNotUsed voice = new VoiceNotUsed();

        voice.listen();
        voice.play();

        try {
            TimeUnit.SECONDS.sleep(10);
//            running = false;
            TimeUnit.MILLISECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        voice.cleanUp();


    }
}
