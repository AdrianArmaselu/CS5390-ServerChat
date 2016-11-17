package edu.utdallas.cs5390.chat.framework.server;

import edu.utdallas.cs5390.chat.framework.common.util.CLIReader;
import edu.utdallas.cs5390.chat.framework.server.service.ServerUDPService;
import edu.utdallas.cs5390.chat.framework.server.service.TCPWelcomeService;

import java.io.IOException;
import java.security.Key;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Adisor on 10/1/2016.
 */

// need to work on the tables for this class
public class ChatServer implements AbstractChatServer {
    private TCPWelcomeService tcpWelcomeService;
    private ServerUDPService serverUdpService;
    private ExecutorService executorService;

    public ChatServer() throws IOException {
        tcpWelcomeService = new TCPWelcomeService(this);
        serverUdpService = new ServerUDPService(this);
        executorService = Executors.newFixedThreadPool(5);
    }

    private void startup() throws IOException {
        tcpWelcomeService.start();
    }

    private void shutdown() {
        tcpWelcomeService.close();
        serverUdpService.close();
    }

    @Override
    public boolean isASubscriber(String username) {
        return false;
    }

    @Override
    public String getUserSecretKey(String username) {
        return null;
    }

    @Override
    public void setId(String username, String address) {

    }

    @Override
    public String getUsername(String ipAddress) {
        return null;
    }

    @Override
    public boolean hasMatchingRes(String username, String cipherKey) {
        return false;
    }

    @Override
    public String getRand(String username) {
        return null;
    }

    @Override
    public void saveRand(String username, String rand) {

    }

    @Override
    public Key generateEncryptionKey(String username) {
        return null;
    }

    @Override
    public void acceptTCPConnectionFromUser(String username) {

    }

    @Override
    public Key getUserEncryptionKey(String ipAddress) {
        return null;
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
                command = cliReader.readInput();
            }
            chatServer.shutdown();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
