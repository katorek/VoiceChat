package wjaronski.socket;

import wjaronski.exception.LogowanieNieudaneException;
import wjaronski.exception.LogowanieUdaneException;

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
//    private BufferedInputStream in;
//    private BufferedOutputStream out;

    private boolean logged = false;

    public SocketConnection(String ip, String port) {
        System.err.println(ip + ":" + port);
        init(ip, port);
    }

    private void init(String ip, String port) {
        try {
            cSocket = new Socket(ip, Integer.parseInt(port));
            out = new PrintWriter(cSocket.getOutputStream(), true);
            klawa = new Scanner(System.in);
            in = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cleanUp() {
        try {
            cSocket.close();
            out.close();
            klawa.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void wyslij(String msg) {
        out.println(msg);
    }

    private void czytajZKlawaituryIWyslij() {
        String msg;
        boolean running = true;
        while (running && klawa.hasNext()) {
            msg = klawa.nextLine();
            if (msg.charAt(0) == '0') {
                msg = "0";
                running = false;
            }
            wyslij(msg);
        }
    }

    private String czytaj() {
        String line = "";
        try {
            line = in.readLine();
        } catch (IOException e) {
            System.err.println("ERR " + line);
        }
        return line;
    }

    public void utworzWatekNasluchujacy() {
        new Thread(() -> {
            boolean running = true;
            String serverMsg;
            System.out.println("Started");
            while (running) {
                serverMsg = czytaj();
                if (serverMsg.equals(ENDING_MSG) || serverMsg.length() == 0) {
                    running = false;
                }
                System.out.print((serverMsg.length() > 0) ? serverMsg + "\n" : "");
            }
        }).start();
    }

    public void establishConnection() {
//        utworzWatekNasluchujacy();
//        czytajZKlawaituryIWyslij();
//        cleanUp();
    }

    public static void main(String[] args) {
        SocketConnection sc = new SocketConnection("127.0.0.1", "12345");
        sc.establishConnection();
//        init();
//        utworzWatekNasluchujacy();
//        czytajZKlawaituryIWyslij();
//        cleanUp();
    }

    public void send(String s) {
        wyslij(s);
    }

    public void loginResponse() throws LogowanieNieudaneException, LogowanieUdaneException {
        String line = czytaj();
        System.out.println(line);
        //1 - accepted, 2-rejected
        if (line.charAt(0) == '2') {
            System.err.println("THROWNIG");
            throw new LogowanieNieudaneException("Nie udane logowanie");
        }
        if (line.charAt(0) == '1') {
            logged = true;
            throw new LogowanieUdaneException();
        }
    }

    public boolean loggedProperly() {
        return logged;
    }

    public Socket getSocket() {
        return cSocket;
    }
}
