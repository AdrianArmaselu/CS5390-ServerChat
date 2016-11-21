package edu.utdallas.cs5390.chat.framework.server.service;

import edu.utdallas.cs5390.chat.framework.common.util.Utils;
import edu.utdallas.cs5390.chat.framework.server.AbstractChatServer;
import edu.utdallas.cs5390.chat.framework.server.ChatServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Adisor on 10/1/2016.
 */

public class TCPWelcomeService extends Thread {
    private static final int SOCKET_WAIT_TIMEOUT = Utils.seconds(1);
    private AbstractChatServer chatServer;
    private ServerSocket serverSocket;

    public TCPWelcomeService(ChatServer chatServer, int port) throws IOException {
        this.chatServer = chatServer;
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(SOCKET_WAIT_TIMEOUT);
    }

    public void run() {
        while (!isInterrupted())
            welcomeClient();
    }

    private void welcomeClient() {
        try {
            Socket clientSocket = serverSocket.accept();
            chatServer.startSession(clientSocket);
        } catch (IOException ignored) {
        }
    }

    public void close() {
        Utils.closeResource(serverSocket);
    }
}
