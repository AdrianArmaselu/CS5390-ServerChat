package edu.utdallas.cs5390.chat.framework.server;

import com.beust.jcommander.JCommander;
import edu.utdallas.cs5390.chat.framework.common.service.TCPMessagingService;
import edu.utdallas.cs5390.chat.framework.common.util.CLIReader;
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

    public ChatServer(ChatServerArguments chatServerArguments) throws IOException {
        tcpWelcomeService = new TCPWelcomeService(this);
        serverUdpService = new ServerUDPService(this);
        executorService = Executors.newFixedThreadPool(5);
        usernameProfiles = new HashMap<>();
        ipProfiles = new HashMap<>();
        loadUserInfo(chatServerArguments.getUsersFile());
    }

    private void startup() throws IOException {
        tcpWelcomeService.start();
    }

    private void shutdown() {
        tcpWelcomeService.close();
        serverUdpService.close();
    }

    private void loadUserInfo(String filename) {
        String line;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            while ((line = bufferedReader.readLine()) != null) {
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
            executorService.execute(tcpMessagingService);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatServerArguments chatServerArguments = new ChatServerArguments();
        JCommander jCommander = new JCommander(chatServerArguments);
        jCommander.parse(args);
        try {
            //setup
            ChatServer chatServer = new ChatServer(chatServerArguments);
            CLIReader cliReader = new CLIReader();
            String command = "";

            // startup
            chatServer.startup();
            while (!command.equals("exit")) {
                command = cliReader.readInput();
            }
            chatServer.shutdown();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
