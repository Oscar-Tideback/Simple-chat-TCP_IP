package Server;

import Clients.Client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private List<ClientHandler> clientslist = new ArrayList<>();
    private ObjectOutputStream writer;
    private Client client;

    public ClientHandler(Socket clientSocket, List userList) throws IOException {
        this.clientSocket = clientSocket;
        this.clientslist = userList;
        writer = new ObjectOutputStream(clientSocket.getOutputStream());
        client = new Client();
    }

    @Override
    public void run() {
        try (ObjectInputStream reader = new ObjectInputStream(clientSocket.getInputStream());) {
            while (true){
               client = (Client) reader.readObject();
                    for (ClientHandler clients : clientslist) {
                        clients.writer.writeObject(client);
                        clients.writer.reset();
                    }
                }
            }  catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}


