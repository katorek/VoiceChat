package wjaronski.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketConnection {
    public static final int LOGOWANIE_UDANE = 1;
    public static final int LOGOWANIE_NIEUDANE = 2;
    public static final int UZYTKOWNIK_JUZ_ZALOGOWANY = 3;
    public static final int UZYTKOWNIK_JUZ_ISNIEJE = 4;
    public static final int UZYTKOWNIK_NIE_ISNIEJE = 5;

    private static Socket cSocket;
    private static PrintWriter out;
    private static BufferedReader in;

    private boolean logged = false;


    public SocketConnection(String ip, String port) {
        System.err.println(ip + ":" + port);
        init(ip, port);
    }

    private void init(String ip, String port) {
        try {
            cSocket = new Socket(ip, Integer.parseInt(port));
            out = new PrintWriter(cSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void wyslij(String msg) {
        out.println(msg);
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

    public void send(String s) {
        wyslij(s);
    }

    public int loginResponse() {
        String response = czytaj();
        return Integer.valueOf(response.charAt(0)+"");
    }

    public boolean loggedProperly() {
        return logged;
    }

    public Socket getSocket() {
        return cSocket;
    }

    public void requestLoggedUsers(){
        wyslij("6");
    }
}
