package edu.utdallas.cs5390.chat.server;

import edu.utdallas.cs5390.chat.CLIReader;
import edu.utdallas.cs5390.chat.server.listeners.ServerTCPListener;
import edu.utdallas.cs5390.chat.util.Utils;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by Adisor on 10/1/2016.
 */

// TODO: do proper udp processing, and organize the code
public class ChatServer extends Thread {
    private static final int SOCKET_WAIT_TIMEOUT = Utils.seconds(1);
    private static final int UDP_PORT = 8080;
    private static final int TCP_PORT = 8081;

    private ServerSocket serverSocket;
    private DatagramSocket datagramSocket;
    private ExecutorService executorService;
    private Map<String, ClientProfile> clientProfileMap;
    private List<ClientService> clients;
    private boolean shutdown;

    public ChatServer() throws IOException {
        executorService = Executors.newFixedThreadPool(5);
        clients = new ArrayList<ClientService>();
        clientProfileMap = new HashMap<String, ClientProfile>();
        datagramSocket = new DatagramSocket(UDP_PORT);
        serverSocket = new ServerSocket(TCP_PORT);
        serverSocket.setSoTimeout(SOCKET_WAIT_TIMEOUT);
    }

    public void run() {
        while (!shutdown)
            processTCPConnections();
    }

    private void processTCPConnections() {
        try {
            Socket clientSocket = serverSocket.accept();
            ClientService clientService = new ClientService(clientSocket);
            clientService.addMessageListener(new ServerTCPListener(this));
            clients.add(clientService);
            executorService.execute(clientService);
        } catch (IOException ignored) {
        }
    }

    private void processUDPConnections() {
        DatagramPacket datagramPacket = new DatagramPacket(new byte[256], 256);
        try {
            datagramSocket.receive(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        shutdown = true;
        for (ClientService clientService : clients) {
            clientService.close();
        }
        if (datagramSocket != null)
            datagramSocket.close();
        if (serverSocket != null)
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public Map<String, ClientProfile> getClientProfileMap() {
        return clientProfileMap;
    }

    public static void main(String[] args) {
        try {
            //setup
            ChatServer chatServer = new ChatServer();
            CLIReader cliReader = new CLIReader();
            String command = "";

            // run
            chatServer.start();
            while (!command.equals("exit")) {
                command = cliReader.getNextCommand();
            }
            chatServer.shutdown();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class UDPServer {
        public UDPServer() {
        }
    }

    class TCPServer {

    }
}
