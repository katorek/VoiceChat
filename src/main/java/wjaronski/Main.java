package wjaronski;

import javafx.application.Application;
import javafx.stage.Stage;
import wjaronski.voice.Speaker;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
//        Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
//        primaryStage.setTitle("Komunikator glosowy");
//        primaryStage.setScene(new Scene(root, 300, 275));
//        primaryStage.show();

//        SoundMenager mic = new SoundMenager();
        Speaker speaker = new Speaker();
//        mic.record();


//        new Thread(() ->{
//            try {
//                TimeUnit.SECONDS.sleep(10);
//                mic.setRunning(false);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();


//        byte[] arr;
//        int bytes;
//        while(mic.isRunning()){
////            mic.lock();
//            arr = mic.getData();
//            bytes = mic.getBytesRead();
////            mic.unlock();
//            speaker.play(arr, bytes);
//        }


//        speaker.clean();
//        mic.clean();

//        voice.cleanUp();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
