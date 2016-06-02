package network;

/**
 * Created by marcin on 07.05.16.
 */
import java.io.IOException;
import java.net.Socket;

public class WaitForClients extends Thread{
    private Server server;

    public WaitForClients(Server server) {
        this.server = server;
    }

    public void run() {
        while (server.isRunning()) {

            // oczekuje na połączenie z klientem
            Socket clientSocket;
            try {
                clientSocket = server.serverSocket.accept();

                // nasłuchuje na klienta w osobnym wątku
                Connection connection = new Connection(clientSocket);
                server.connections.add(connection);
                connection.start();
            } catch (IOException e) {
            }
        }
    }
}
