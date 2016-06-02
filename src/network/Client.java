package network;

/**
 * Created by marcin on 07.05.16.
 */
import java.net.Socket;

public class Client {
    private String host;

    private int port;

    private String playerID;

    private Socket socket;

    private Connection connection;

    public Client(String nick, String host, int port) {
        this.host = host;
        this.port = port;
        this.playerID = nick;
    }

    public boolean start() {
        try {
            socket = new Socket(host, port);
        } catch (Exception ex) {
            return false;
        }

        connection = new Connection(socket);
        connection.start();

        return true;
    }

    public void stop() {
        connection.close();
    }

    public String getPlayerID() {
        return playerID;
    }

    public void sendMessage(GameEvent ge) {
        connection.sendMessage(ge.toSend());
    }

    public GameEvent receiveMessage() {
        if (connection.messagesQueue.isEmpty()) {
            return null;
        } else {
            GameEvent ge = new GameEvent((String) connection.messagesQueue
                    .getFirst());
            connection.messagesQueue.removeFirst();
            return ge;
        }
    }

    public boolean isAlive() {
        return connection.isAlive();
    }
}
