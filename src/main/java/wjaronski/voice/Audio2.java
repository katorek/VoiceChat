//package wjaronski.voice;
//
//import javax.sound.sampled.AudioFormat;
//import javax.sound.sampled.AudioInputStream;
//import javax.sound.sampled.AudioSystem;
//import javax.sound.sampled.DataLine;
//import javax.sound.sampled.Mixer;
//import javax.sound.sampled.SourceDataLine;
//import javax.sound.sampled.TargetDataLine;
//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.net.Socket;
//
//public class Audio2 {
//    boolean stopCapture = false;
//    ByteArrayOutputStream byteArrayOutputStream;
//    AudioFormat audioFormat;
//    TargetDataLine targetDataLine;
//    AudioInputStream audioInputStream;
//    BufferedOutputStream out = null;
//    BufferedInputStream in = null;
//    Socket sock = null;
//    SourceDataLine sourceDataLine;
//
//    public static void main(String[] args) {
//        Audio2 tx = new Audio2();
//        tx.captureAudio();
//    }
//
//
//
//    private void captureAudio() {
//        try {
//            sock = new Socket("127.0.0.1", 1234);
//            out = new BufferedOutputStream(sock.getOutputStream());
//            in = new BufferedInputStream(sock.getInputStream());
//
//            audioFormat = getAudioFormat();
//
//            DataLine.Info dataLineInfo = new DataLine.Info(
//                    TargetDataLine.class, audioFormat);
//
//            Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
//            System.out.println("Available mixers:");
//            Mixer mix;
//            for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
//                mix = AudioSystem.getMixer(mixerInfo[cnt]);
//                if (mix.isLineSupported(dataLineInfo)) {
//                    System.out.println(mixerInfo[cnt].getName());
//                    targetDataLine = (TargetDataLine) mix.getLine(dataLineInfo);
//                }
////                System.out.println(mixerInfo[cnt].getName());
//            }
//
//
//            Mixer mixer = AudioSystem.getMixer(mixerInfo[3]);
//
//            targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
//
//            targetDataLine.open(audioFormat);
//            targetDataLine.start();
//
//            Thread captureThread = new CaptureThread();
//            captureThread.start();
//
//            DataLine.Info dataLineInfo1 = new DataLine.Info(
//                    SourceDataLine.class, audioFormat);
//            sourceDataLine = (SourceDataLine) AudioSystem
//                    .getLine(dataLineInfo1);
//            sourceDataLine.open(audioFormat);
//            sourceDataLine.start();
//
//            Thread playThread = new PlayThread();
//            playThread.start();
//
//        } catch (Exception e) {
//            System.out.println(e);
//            System.exit(0);
//        }
//    }
//
//    class CaptureThread extends Thread {
//
//        byte tempBuffer[] = new byte[1000];
//
//        @Override
//        public void run() {
////            byteArrayOutputStream = new ByteArrayOutputStream();
//            stopCapture = false;
//            try {
//                while (!stopCapture) {
//
//                    int cnt = targetDataLine.read(tempBuffer, 0,
//                            tempBuffer.length);
//
//                    out.write(tempBuffer);
//
////                    if (cnt > 0) {
////                        byteArrayOutputStream.write(tempBuffer, 0, cnt);
////                    }
//                }
////                byteArrayOutputStream.close();
//            } catch (Exception e) {
//                System.out.println(e);
//                System.exit(0);
//            }
//        }
//    }
//
//    private AudioFormat getAudioFormat() {
//        float sampleRate = 8000.0F;
//
//        int sampleSizeInBits = 8;
//
//        int channels = 1;
//
//        boolean signed = true;
//
//        boolean bigEndian = false;
//
//        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed,
//                bigEndian);
//    }
//
//    class PlayThread extends Thread {
//
//        byte tempBuffer[] = new byte[1000];
//
//        @Override
//        public void run() {
//            try {
//                while (in.read(tempBuffer) != -1) {
//                    sourceDataLine.write(tempBuffer, 0, 1000);
//
//                }
//                sourceDataLine.drain();
//                sourceDataLine.close();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}
