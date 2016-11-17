package edu.utdallas.cs5390.chat.server.service;

import edu.utdallas.cs5390.chat.common.connection.EncryptedTCPConnection;
import edu.utdallas.cs5390.chat.common.service.TCPMessagingService;
import edu.utdallas.cs5390.chat.common.util.Utils;
import edu.utdallas.cs5390.chat.server.AbstractChatServer;
import edu.utdallas.cs5390.chat.server.ChatServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Adisor on 10/1/2016.
 */

public class TCPWelcomeService extends Thread {
    private static final int SOCKET_WAIT_TIMEOUT = Utils.seconds(1);
    private static final int TCP_PORT = 8081;

    private AbstractChatServer chatServer;
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
            TCPMessagingService tcpMessagingService = new TCPMessagingService(clientSocket, chatServer.getUserEncryptionKey(clientSocket.getInetAddress().getHostAddress()));
            tcpMessagingService.addCustomProtocol();
            tcpMessagingService.start();
        } catch (IOException ignored) {
        }
    }

    public void close() {
        Utils.closeResource(serverSocket);
    }
}
