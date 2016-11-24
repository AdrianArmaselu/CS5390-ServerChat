package edu.utdallas.cs5390.chat.framework.server;

import edu.utdallas.cs5390.chat.framework.common.ContextValues;
import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.service.TCPMessagingService;
import edu.utdallas.cs5390.chat.framework.common.util.Utils;
import edu.utdallas.cs5390.chat.framework.server.service.ClientProfile;
import edu.utdallas.cs5390.chat.framework.server.service.HistoryMessage;
import edu.utdallas.cs5390.chat.framework.server.service.ServerUDPService;
import edu.utdallas.cs5390.chat.framework.server.service.TCPWelcomeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Logger logger = LoggerFactory.getLogger(ChatServer.class);
    private TCPWelcomeService tcpWelcomeService;
    private ServerUDPService serverUdpService;
    private ExecutorService executorService;
    private Map<String, ClientProfile> usernameProfiles;
    private Map<String, ClientProfile> addressProfiles;
    private Map<String, ContextualProtocol> cliProtocols;
    private Map<String, ContextualProtocol> tcpProtocols;
    private Map<String, TCPMessagingService> usersService;

    public ChatServer(ChatServerArguments chatServerArguments) throws IOException {
        tcpWelcomeService = new TCPWelcomeService(this, chatServerArguments.getTcpPort());
        serverUdpService = new ServerUDPService(this, chatServerArguments.getUdpPort());
        executorService = Executors.newFixedThreadPool(5);
        usernameProfiles = new HashMap<>();
        addressProfiles = new HashMap<>();
        cliProtocols = new HashMap<>();
        tcpProtocols = new HashMap<>();
        usersService = new HashMap<>();
        loadUserInfo(chatServerArguments.getUsersFile());
        logger.debug("Loaded users from file " + usernameProfiles.toString());
    }

    public void startup() {
        tcpWelcomeService.start();
        serverUdpService.start();
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
    public ClientProfile getProfileByAddress(String address) {
        return addressProfiles.get(address);
    }

    @Override
    public void addAddressProfile(String address, ClientProfile usernameProfile) {
        addressProfiles.put(address, usernameProfile);
    }

    @Override
    public void startSession(Socket clientSocket) {
        try {
            String ip = clientSocket.getInetAddress().getHostAddress();
            ClientProfile clientProfile = null;
            for (Map.Entry<String, ClientProfile> profileEntry : addressProfiles.entrySet()) {
                String address = profileEntry.getKey();
                if (address.substring(0, address.indexOf(":")).equals(ip) && profileEntry.getValue().isRegistered && !profileEntry.getValue().isConnected) {
                    clientProfile = profileEntry.getValue();
                    clientProfile.isConnected = true;
                    break;
                }
            }
            TCPMessagingService tcpMessagingService = new TCPMessagingService(clientSocket, clientProfile.encryptionKey);
            tcpMessagingService.username = clientProfile.username;
            addTcpProtocolsToTcpMessagingService(tcpMessagingService, clientProfile);
            usersService.put(clientProfile.username, tcpMessagingService);
            ClientProfile finalClientProfile = clientProfile;
            tcpMessagingService.setOnChatMessageProtocol(new ContextualProtocol() {
                @Override
                public void executeProtocol() {
                    String message = (String) getContextValue(ContextValues.message);
                    usersService.get(finalClientProfile.partner).queueMessage(message);
                    HistoryMessage historyMessage = new HistoryMessage(finalClientProfile.sessionId, finalClientProfile.username, finalClientProfile.partner, message);
                    finalClientProfile.storeMessage(historyMessage);
                    usernameProfiles.get(finalClientProfile.partner).storeMessage(historyMessage);
                }
            });
            executorService.execute(tcpMessagingService);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTcpProtocolsToTcpMessagingService(TCPMessagingService tcpMessagingService, ClientProfile clientProfile) {
        tcpProtocols.forEach((tcpMessage, tcpProtocol) -> {
                    try {
                        // we want each messaging service to have a unique instance
                        ContextualProtocol protocolCopy = tcpProtocol.getClass().newInstance();
                        protocolCopy.setContextValue(ContextValues.clientProfile, clientProfile);
                        protocolCopy.setContextValue(ContextValues.tcpMessagingService, tcpMessagingService);
                        protocolCopy.setContextValue(ContextValues.chatServer, this);
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

    @Override
    public TCPMessagingService getTcpMessagingService(String targetUsername) {
        return usersService.get(targetUsername);
    }

}
