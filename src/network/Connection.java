package network;

/**
 * Created by marcin on 07.05.16.
 */
import java.io.*;
import java.net.Socket;
import java.util.LinkedList;

public class Connection extends Thread{
    private String nick = "";

    private boolean isJoined = false;

    private Socket socket;

    private BufferedReader in;

    private PrintWriter out;

    public LinkedList<String> messagesQueue;

    public Connection(Socket socket) {
        try {
            in = new BufferedReader(new InputStreamReader(socket
                    .getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException ex) {
        }
        messagesQueue = new LinkedList<String>();
        this.socket = socket;
    }

    public void sendMessage(String s) {
        out.println(s);
    }

    public void run() {
        String s;
        try {
            while ((s = in.readLine()) != null) {
                messagesQueue.add(s);
            }
            out.close();
            in.close();
        } catch (IOException ex) {
        }

        try {
            socket.close();
        } catch (Exception ex) {
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (Exception ex) {
        }
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getNick() {
        return nick;
    }

    public void setJoined(boolean b) {
        isJoined = b;
    }

    public boolean isJoined() {
        return isJoined;
    }
}
