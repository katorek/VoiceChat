package wjaronski.voice;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class Audio {
    public static void main(String[] args) {
        Audio a = new Audio();
        a.captureAudio();

    }
    public boolean stopCapture = false;

    ByteArrayOutputStream byteArrayOutputStream;
    AudioFormat audioFormat;
    TargetDataLine targetDataLine;
    AudioInputStream audioInputStream;
    SourceDataLine sourceDataLine;

    private void captureAudio(){
        try{
            //Get and display a list of
            // available mixers.
            Mixer.Info[] mixerInfo =
                    AudioSystem.getMixerInfo();
            System.out.println("Available mixers:");
            for(int cnt = 0; cnt < mixerInfo.length;
                cnt++){
                System.out.println(mixerInfo[cnt].
                        getName());
            }//end for loop

            //Get everything set up for capture
            audioFormat = getAudioFormat();

            DataLine.Info dataLineInfo =
                    new DataLine.Info(
                            TargetDataLine.class,
                            audioFormat);

            //Select one of the available
            // mixers.
            Mixer mixer = AudioSystem.
                    getMixer(mixerInfo[3]);

            //Get a TargetDataLine on the selected
            // mixer.
            targetDataLine = (TargetDataLine)
                    mixer.getLine(dataLineInfo);
            //Prepare the line for use.
            targetDataLine.open(audioFormat);
            targetDataLine.start();

            //Create a thread to capture the microphone
            // data and start it running.  It will run
            // until the Stop button is clicked.
            new CaptureThread().start();
            TimeUnit.SECONDS.sleep(5);
            playAudio();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        //8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;
        //8,16
        int channels = 1;
        //1,2
        boolean signed = true;
        //true,false
        boolean bigEndian = false;
        //true,false
        return new AudioFormat(
                sampleRate,
                sampleSizeInBits,
                channels,
                signed,
                bigEndian);
    }

    private void playAudio() {
        try{
            //Get everything set up for playback.
            //Get the previously-saved data into a byte
            // array object.
            byte audioData[] = byteArrayOutputStream.
                    toByteArray();
            //Get an input stream on the byte array
            // containing the data
            InputStream byteArrayInputStream =
                    new ByteArrayInputStream(audioData);
            AudioFormat audioFormat = getAudioFormat();
            audioInputStream = new AudioInputStream(
                    byteArrayInputStream,
                    audioFormat,
                    audioData.length/audioFormat.
                            getFrameSize());
            DataLine.Info dataLineInfo =
                    new DataLine.Info(
                            SourceDataLine.class,
                            audioFormat);
            sourceDataLine = (SourceDataLine)
                    AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();

            //Create a thread to play back the data and
            // start it  running.  It will run until
            // all the data has been played back.
            Thread playThread = new PlayThread();
            playThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }//end catch
    }


    class CaptureThread extends Thread {
        byte tempBuffer[] = new byte[10000];
        public void run() {
            byteArrayOutputStream = new ByteArrayOutputStream();
            stopCapture = false;
            try {
                while (!stopCapture) {
                    //Read data from the internal buffer of
                    // the data line.
                    int cnt = targetDataLine.read(tempBuffer,
                            0,
                            tempBuffer.length);
                    if (cnt > 0) {
                        //Save data in output stream object.
                        byteArrayOutputStream.write(tempBuffer,
                                0,
                                cnt);
                    }
                }
                byteArrayOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class PlayThread extends Thread {
        byte tempBuffer[] = new byte[10000];

        public void run() {
            try {
                int cnt;
                //Keep looping until the input read method
                // returns -1 for empty stream.
                while ((cnt = audioInputStream.read(
                        tempBuffer, 0,
                        tempBuffer.length)) != -1) {
                    if (cnt > 0) {
                        //Write data to the internal buffer of
                        // the data line where it will be
                        // delivered to the speaker.
                        sourceDataLine.write(tempBuffer, 0, cnt);
                    }
                }
                sourceDataLine.drain();
                sourceDataLine.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
