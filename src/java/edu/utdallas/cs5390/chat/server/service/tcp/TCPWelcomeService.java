package edu.utdallas.cs5390.chat.server.service.tcp;

import edu.utdallas.cs5390.chat.server.ChatServer;
import edu.utdallas.cs5390.chat.util.Utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Adisor on 10/1/2016.
 */

// we can try and decouple this from the ChatServer by making ChatServer functionalities static
public class TCPWelcomeService extends Thread {
    private static final int SOCKET_WAIT_TIMEOUT = Utils.seconds(1);
    private static final int TCP_PORT = 8081;

    private ChatServer chatServer;
    private ServerSocket serverSocket;

    public TCPWelcomeService(ChatServer chatServer) throws IOException {
        this.chatServer = chatServer;
        serverSocket = new ServerSocket(TCP_PORT);
        serverSocket.setSoTimeout(SOCKET_WAIT_TIMEOUT);
    }

    public void run() {
        while (!isInterrupted()) {
            welcomeClient();
        }
    }

    private void welcomeClient() {
        try {
            Socket clientSocket = serverSocket.accept();
            chatServer.serviceClient(clientSocket);
        } catch (IOException ignored) {
        }
    }

    public void close() {
        Utils.closeResource(serverSocket);
    }
}
