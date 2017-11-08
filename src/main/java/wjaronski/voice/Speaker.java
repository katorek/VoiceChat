package wjaronski.voice;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Speaker {
    private AudioFormat format;
    private SourceDataLine speakers;

    public Speaker(){
        init();
    }

    private void init(){
        format = new AudioFormat(8000.0f, 16, 1, true, true);
        try {
            speakers = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, format));
            speakers.open(format);
            speakers.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void play(byte[] data, int bytesRead){
        speakers.write(data, 0, bytesRead);
    }

    public void clean(){
        speakers.drain();
        speakers.close();
    }
}
