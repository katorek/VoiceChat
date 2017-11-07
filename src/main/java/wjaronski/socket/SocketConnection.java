package wjaronski.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class SocketConnection {
    private static final String ENDING_MSG = "0\n";

    private static Socket cSocket;
    private static Scanner klawa;
    private static PrintWriter out;
    private static BufferedReader in;

    private static void init() {
        try {
            cSocket = new Socket("127.0.0.1", 1234);
            out = new PrintWriter(cSocket.getOutputStream(), true);
            klawa = new Scanner(System.in);
            in = new BufferedReader(
                    new InputStreamReader(cSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void cleanUp() {
        try {
            cSocket.close();
            out.close();
            klawa.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void czytajZKlawaituryIWyslij() {
        String msg;
        boolean running = true;
        while (running && klawa.hasNext()) {
            msg = klawa.nextLine();
            if (!isEmptyMessage(msg)) {
                if (isEndMessage(msg)) {
                    msg = "0";
                    running = false;
                }
                wyslij(msg);
            }
        }
    }

    private static boolean isEmptyMessage(String msg) {
        return msg == null || msg.length() == 0;
    }

    private static boolean isEndMessage(String msg) {
        return msg.charAt(0) == '0';
    }

    private static void wyslij(String msg) {
        out.println(msg);
    }

    private static String czytaj() {
        String line = "";
        try {
            line = in.readLine();
        } catch (IOException e) {
            System.err.println("ERR " + line);
        }
        return line;
    }

    private static void utworzWatekCzytajacy() {
        new Thread(() -> {
            boolean running = true;
            String serverMsg;
            System.out.println("Started");
            while (running) {
                serverMsg = czytaj();
                if (serverMsg.equals(ENDING_MSG) || serverMsg.length() == 0) {
                    System.out.println("Ending");
                    running = false;
                }
                System.out.print((serverMsg.length() > 0) ? serverMsg + "\n" : "");
            }
        }).start();
    }

    public static void main(String[] args) {
        init();
        utworzWatekCzytajacy();
        czytajZKlawaituryIWyslij();
        cleanUp();
    }
}
