package Server;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server extends Thread {
    private int serverPort;
    private List<ClientHandler> clientslist = new ArrayList<>();

    public Server(int serverPort) {this.serverPort = serverPort;}

    public static void main(String[] args) {
        Server server = new Server(12345);
        server.start();
    }

    public void run() {
        while (true) {
            try (ServerSocket serverSocket = new ServerSocket(12345)) {
                final Socket socketToClient = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socketToClient, clientslist);
                clientslist.add(clientHandler);
                clientHandler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
