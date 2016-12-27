package edu.utdallas.cs5390.chat.framework.server;

import com.beust.jcommander.JCommander;
import edu.utdallas.cs5390.chat.framework.common.ChatPacket;
import edu.utdallas.cs5390.chat.framework.common.connection.EncryptedTcpConnection;
import edu.utdallas.cs5390.chat.framework.common.connection.UdpConnection;
import edu.utdallas.cs5390.chat.framework.common.service.MessagingService;
import edu.utdallas.cs5390.chat.framework.common.util.CLIReader;
import edu.utdallas.cs5390.chat.framework.common.util.Utils;
import edu.utdallas.cs5390.chat.framework.server.service.ClientProfile;
import edu.utdallas.cs5390.chat.framework.server.service.HistoryMessage;
import edu.utdallas.cs5390.chat.framework.server.service.TCPWelcomeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Adisor on 10/1/2016.
 */
// TODO: setup history for multiple session. What happens if there is no history?
    // TODO: WHAT IF WRONG USERNAME IS PROVIDED FOR HISTORY?
public class ChatServer {
    private final Logger logger = LoggerFactory.getLogger(ChatServer.class);
    private TCPWelcomeService tcpWelcomeService;
    private UdpService udpService;
    private ExecutorService executorService;
    private Map<String, ClientProfile> usernameProfiles;
    private Map<String, ClientProfile> addressProfiles;
    private Map<String, MessagingService> usersService;

    public ChatServer(ChatServerArguments chatServerArguments) throws IOException {
        tcpWelcomeService = new TCPWelcomeService(this, chatServerArguments.getTcpPort());
        udpService = new UdpService(chatServerArguments.getUdpPort());
        executorService = Executors.newFixedThreadPool(100);
        usernameProfiles = new HashMap<>();
        addressProfiles = new HashMap<>();
        usersService = new HashMap<>();
        loadUserInfo(chatServerArguments.getUsersFile());
        logger.debug("Loaded users from file " + usernameProfiles.toString());
    }

    public void startup() {
        tcpWelcomeService.start();
        udpService.start();
    }

    public void shutdown() {
        tcpWelcomeService.close();
        udpService.close();
        System.exit(0);
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

    public void startSession(Socket clientSocket) {
        try {
            EncryptedTcpConnection connection = new EncryptedTcpConnection(clientSocket, null);
            logger.debug("Created tcp connection with the client");
            int previousPort = Integer.parseInt(connection.receiveMessage());
            String ipAddress = connection.getIpAddress();
            ClientProfile clientProfile = addressProfiles.get(ipAddress + ":" + previousPort);
            connection.setEncryptionKey(clientProfile.encryptionKey);
            TcpService tcpService = new TcpService(clientProfile, connection);
            usersService.put(clientProfile.username, tcpService);
            executorService.execute(tcpService);
            clientProfile.isRegistered = true;
            tcpService.queueMessage("REGISTERED");
        } catch (IOException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
    }

    private class TcpService extends MessagingService {
        ClientProfile clientProfile;
        ClientProfile corespondentProfile;

        TcpService(ClientProfile clientProfile, EncryptedTcpConnection encryptedTcpConnection) {
            super(encryptedTcpConnection);
            this.clientProfile = clientProfile;
        }

        void setCorespondentProfile(ClientProfile corespondentProfile) {
            this.corespondentProfile = corespondentProfile;
        }

        @Override
        protected void processNextMessage() throws Exception {
            try {
                String message = retrieveMessage();
                if (message == null || message.equals("")) shutdown();
                if (Utils.hasHeader(message)) {
                    String header = Utils.extractHeader(message);
                    switch (header) {
                        case "CONNECT":
                            String corespondent = Utils.extractValue(message);
                            corespondentProfile = usernameProfiles.get(corespondent);
                            logger.debug("Retrieved corespondent profile " + corespondentProfile);
                            boolean isCorrespondentReachable = corespondentProfile != null && corespondentProfile.isRegistered && !corespondentProfile.inSession;
                            if (isCorrespondentReachable) {
                                String sessionId = Utils.UUID();
                                TcpService corespondentService = (TcpService) usersService.get(corespondent);
                                clientProfile.sessionId = sessionId;
                                corespondentProfile.sessionId = sessionId;
                                corespondentService.setCorespondentProfile(clientProfile);
                                queueMessage("START(" + sessionId + "," + corespondent + ")");
                                corespondentService.queueMessage("START(" + sessionId + "," + clientProfile.username + ")");
                            } else {
                                queueMessage("UNREACHABLE(" + corespondent + ")");
                            }
                            break;
                        case "HISTORY_REQ":
                            queueMessage("HISTORY_RESP(" + clientProfile.sessionId + "," + clientProfile.getHistory() + ")");
                            break;
                        case "END_REQUEST":
                            String sessionId = clientProfile.sessionId;
                            queueMessage("END_NOTIF(" + sessionId + ")");
                            TcpService tcpService = (TcpService) usersService.get(corespondentProfile.username);
                            tcpService.queueMessage("END_NOTIF(" + sessionId + ")");
                            break;
                        default:
                            logger.info("Sending message to " + corespondentProfile.username);
                            usersService.get(corespondentProfile.username).queueMessage(message);
                            HistoryMessage historyMessage = new HistoryMessage(clientProfile.sessionId, clientProfile.username, corespondentProfile.username, Utils.extractValues(message).get(1));
                            clientProfile.storeMessage(historyMessage);
                            corespondentProfile.storeMessage(historyMessage);
                    }
                }
            }catch (Exception e){
                if(e.getMessage().equals("Connection reset")){
                    String sessionId = clientProfile.sessionId;
                    TcpService tcpService = (TcpService) usersService.get(corespondentProfile.username);
                    tcpService.queueMessage("END_NOTIF(" + sessionId + ")");
                }
                throw  new Exception(e.getMessage(), e);
            }
        }
    }

    private class UdpService extends Thread {
        private DatagramSocket datagramSocket;
        private Map<String, UdpConnection> clientConnections;
        private ChatPacket chatPacket;
        private String clientAddress;
        private String clientIp;
        private int clientPort;
        private String message;
        private ClientProfile clientProfile;

        UdpService(int port) throws SocketException {
            clientConnections = new HashMap<>();
            datagramSocket = new DatagramSocket(port);
        }

        public void run() {
            while (!isInterrupted()) {
                try {
                    unmarshallingRequest();
                    if (!verifyClient()) continue;
                    processRequest();
                } catch (Exception e) {
                    if (!e.getMessage().equals("Receive timed out"))
                        logger.error(e.getMessage(), e);
                }
            }
        }

        private void unmarshallingRequest() throws IOException {
            DatagramPacket receivedPacket = receivePacket();
            clientIp = getCorespondentIp(receivedPacket);
            clientPort = receivedPacket.getPort();
            clientAddress = clientIp + ":" + clientPort;
            chatPacket = new ChatPacket(receivedPacket.getData());
        }

        private boolean verifyClient() throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, SocketException, UnknownHostException {
            boolean firstTimeConnecting = !clientConnections.containsKey(clientAddress);
            boolean isASubscriber = true;
            if (firstTimeConnecting) isASubscriber = establishSubscription();
            else message = chatPacket.getMessage(clientProfile.encryptionKey);
            return isASubscriber;
        }

        private boolean establishSubscription() throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, SocketException, UnknownHostException {
            clientConnections.put(clientAddress, new UdpConnection(datagramSocket, clientIp, clientPort));
            message = chatPacket.getMessage(null);
            String username = Utils.extractValue(message);
            clientProfile = usernameProfiles.get(username);
            addressProfiles.put(clientAddress, clientProfile);
            return clientProfile != null;
        }

        private DatagramPacket receivePacket() throws IOException {
            byte[] buffer = new byte[1024];
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
            datagramSocket.receive(receivedPacket);
            return receivedPacket;
        }

        private String getCorespondentIp(DatagramPacket datagramPacket) {
            return datagramPacket.getAddress().getHostAddress();
        }

        private void processRequest() throws Exception {
            String header = Utils.extractHeader(message);
            UdpConnection udpConnection = clientConnections.get(clientAddress);
            clientProfile = addressProfiles.get(clientAddress);
            switch (header) {
                case "HELLO":
                    String rand = Utils.randomString(4);
                    clientProfile.createXRes(rand);
                    String response = "CHALLENGE(" + rand + ")";
                    udpConnection.sendMessage(response);
                    break;
                case "RESPONSE":
                    String res = Utils.extractValue(message);
                    boolean resMatch = res.equals(clientProfile.xRes);
                    if (resMatch) {
                        clientProfile.createEncryptionKey();
                        udpConnection.addEncryption(clientProfile.encryptionKey);
                        udpConnection.sendMessage("AUTH_SUCCESS");
                    } else {
                        udpConnection.sendMessage("AUTH_FAIL");
                    }
                    break;
                case "REGISTER":
                    addressProfiles.get(clientAddress).isRegistered = true;
                    break;
            }
        }

        void close() {
            Utils.closeResource(datagramSocket);
        }
    }

    public static void main(String[] args) {
        ChatServerArguments chatServerArguments = new ChatServerArguments();
        JCommander cmdParser = new JCommander(chatServerArguments);
        try {
            cmdParser.parse(args);
        } catch (Exception ignored) {
            cmdParser.usage();
            System.exit(0);
        }
        try {
            ChatServer chatServer = new ChatServer(chatServerArguments);
            chatServer.startup();
            CLIReader cliReader = new CLIReader();
            if (cliReader.readInput().equals("exit"))
                chatServer.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
