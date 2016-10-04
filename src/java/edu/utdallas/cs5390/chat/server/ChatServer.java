package edu.utdallas.cs5390.chat.server;

import edu.utdallas.cs5390.chat.CLIReader;
import edu.utdallas.cs5390.chat.server.processor.ClientMessageProcessor;
import edu.utdallas.cs5390.chat.server.service.ClientMessagingService;
import edu.utdallas.cs5390.chat.server.service.ClientProfile;
import edu.utdallas.cs5390.chat.server.service.tcp.TCPWelcomeService;
import edu.utdallas.cs5390.chat.server.processor.RegisterProcessor;
import edu.utdallas.cs5390.chat.server.service.udp.UDPConnectionService;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Adisor on 10/1/2016.
 */

// need to work on the tables for this class
public class ChatServer {
    private Map<String, ClientProfile> clientProfiles;
    private Map<String, String> usernameIPTable;
    private Map<String, ClientMessagingService> clientMessagingServices;
    private TCPWelcomeService tcpWelcomeService;
    private UDPConnectionService udpConnectionService;
    private ExecutorService executorService;

    public ChatServer() throws IOException {
        clientProfiles = new HashMap<String, ClientProfile>();
        clientMessagingServices = new HashMap<String, ClientMessagingService>();
        tcpWelcomeService = new TCPWelcomeService(this);
        udpConnectionService = new UDPConnectionService();
        udpConnectionService.addPacketListener(new RegisterProcessor(this));
        executorService = Executors.newFixedThreadPool(5);
    }

    private void startup() throws IOException {
        tcpWelcomeService.start();
        udpConnectionService.start();
    }

    public ClientMessagingService getClientMessagingService(String userID) {
        return clientMessagingServices.get(userID);
    }

    public void serviceClient(Socket clientSocket) throws IOException {
        ClientMessagingService clientMessagingService = new ClientMessagingService(clientSocket);
        clientMessagingServices.put("username", clientMessagingService);
        clientMessagingService.addMessageProcessor(new ClientMessageProcessor(this));
        executorService.execute(clientMessagingService);
    }

    public void logOffUser(String userID){
        ClientMessagingService clientMessagingService = clientMessagingServices.remove(userID);
        clientMessagingService.close();
    }

    private void shutdown() {
        for (ClientMessagingService clientMessagingService : clientMessagingServices.values())
            clientMessagingService.close();
        tcpWelcomeService.close();
        udpConnectionService.close();
    }

    public static void main(String[] args) {
        try {
            //setup
            ChatServer chatServer = new ChatServer();
            CLIReader cliReader = new CLIReader();
            String command = "";

            // startup
            chatServer.startup();
            while (!command.equals("exit")) {
                command = cliReader.getNextCommand();
            }
            chatServer.shutdown();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
