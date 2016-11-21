package edu.utdallas.cs5390.chat.framework.server;

import edu.utdallas.cs5390.chat.framework.common.ContextValues;
import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.service.TCPMessagingService;
import edu.utdallas.cs5390.chat.framework.common.util.Utils;
import edu.utdallas.cs5390.chat.framework.server.service.ClientProfile;
import edu.utdallas.cs5390.chat.framework.server.service.ServerUDPService;
import edu.utdallas.cs5390.chat.framework.server.service.TCPWelcomeService;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Adisor on 10/1/2016.
 */

public class ChatServer implements AbstractChatServer {
    private TCPWelcomeService tcpWelcomeService;
    private ServerUDPService serverUdpService;
    private ExecutorService executorService;
    private Map<String, ClientProfile> usernameProfiles;
    private Map<String, ClientProfile> ipProfiles;
    private Map<String, ContextualProtocol> cliProtocols;
    private Map<String, ContextualProtocol> tcpProtocols;
    private Map<String, TCPMessagingService> usersService;

    public ChatServer(ChatServerArguments chatServerArguments) throws IOException {
        tcpWelcomeService = new TCPWelcomeService(this, chatServerArguments.getTcpPort());
        serverUdpService = new ServerUDPService(chatServerArguments.getUdpPort());
        executorService = Executors.newFixedThreadPool(5);
        usernameProfiles = new HashMap<>();
        ipProfiles = new HashMap<>();
        cliProtocols = new HashMap<>();
        tcpProtocols = new HashMap<>();
        usersService = new HashMap<>();
        loadUserInfo(chatServerArguments.getUsersFile());
        System.out.println("Server initialized");
    }

    public void startup() {
        tcpWelcomeService.start();
        serverUdpService.start();
        System.out.println("Server up and Running");
    }

    public void shutdown() {
        tcpWelcomeService.close();
        serverUdpService.close();
    }

    private void loadUserInfo(String filename) {
        String line;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            while ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
                String[] words = line.split(" ");
                ClientProfile clientProfile = new ClientProfile();
                clientProfile.username = words[0];
                clientProfile.password = words[1];
                usernameProfiles.put(clientProfile.username, clientProfile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Utils.closeResource(bufferedReader);
        }
    }

    @Override
    public boolean isASubscriber(String username) {
        return usernameProfiles.containsKey(username);
    }

    public ClientProfile getProfileByUsername(String username) {
        return usernameProfiles.get(username);
    }

    @Override
    public ClientProfile getProfileByIp(String ipAddress) {
        return ipProfiles.get(ipAddress);
    }

    @Override
    public void addIpProfile(String ipAddress, ClientProfile usernameProfile) {
        ipProfiles.put(ipAddress, usernameProfile);
    }

    @Override
    public void startSession(Socket clientSocket) {
        try {
            ClientProfile clientProfile = getProfileByIp(clientSocket.getInetAddress().getHostAddress());
            TCPMessagingService tcpMessagingService = new TCPMessagingService(clientSocket, clientProfile.encryptionKey);
            addTcpProtocolsToTcpMessagingService(tcpMessagingService, clientProfile);
            usersService.put(clientProfile.username, tcpMessagingService);
            executorService.execute(tcpMessagingService);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTcpProtocolsToTcpMessagingService(TCPMessagingService tcpMessagingService, ClientProfile clientProfile){
        tcpProtocols.forEach((tcpMessage, tcpProtocol) -> {
                    try {
                        // we want each messaging service to have a unique instance
                        ContextualProtocol protocolCopy = tcpProtocol.getClass().newInstance();
                        protocolCopy.setContextValue(ContextValues.clientProfile, clientProfile);
                        protocolCopy.setContextValue(ContextValues.tcpMessagingService, tcpMessagingService);
                        tcpMessagingService.addProtocol(tcpMessage, protocolCopy);
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    @Override
    public void addTcpProtocol(String tcpMessage, ContextualProtocol tcpProtocol) {
        tcpProtocols.put(tcpMessage, tcpProtocol);
    }

    @Override
    public void addUdpProtocol(String udpMessage, ContextualProtocol udpProtocol) {
        serverUdpService.addProtocol(udpMessage, udpProtocol);
    }

    public ServerUDPService getUdpService() {
        return serverUdpService;
    }

    @Override
    public TCPMessagingService getTcpMessagingService(String targetUsername) {
        return usersService.get(targetUsername);
    }
}
