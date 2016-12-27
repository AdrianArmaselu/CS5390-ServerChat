package edu.utdallas.cs5390.chat.framework.client;

import com.beust.jcommander.JCommander;
import edu.utdallas.cs5390.chat.framework.common.connection.EncryptedTcpConnection;
import edu.utdallas.cs5390.chat.framework.common.connection.UdpConnection;
import edu.utdallas.cs5390.chat.framework.common.service.MessagingService;
import edu.utdallas.cs5390.chat.framework.common.util.CLIReader;
import edu.utdallas.cs5390.chat.framework.common.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Created by Adisor on 10/1/2016.
 */
public class ChatClient {
    private final Logger logger = LoggerFactory.getLogger(ChatClient.class);
    private UdpConnection udpConnection;
    private CLIReader cliReader;
    private String username;
    private String password;
    private String serverIpAddress;
    private int tcpPort;
    private MessagingService tcpMessagingService;
    private String sessionId;
    private String corespondent;
    private boolean isRegistered;

    private ChatClient(ChatClientArguments chatClientArguments) throws IOException {
        username = chatClientArguments.getUsername();
        password = chatClientArguments.getPassword();
        serverIpAddress = chatClientArguments.getServerAddress();
        tcpPort = chatClientArguments.getTcpPort();
        udpConnection = new UdpConnection(chatClientArguments.getServerAddress(), chatClientArguments.getUdpServerPort());

        cliReader = new CLIReader();
    }

    private void run() {
        logger.info("started client");
        String message = "";
        while (!message.equals("exit"))
            executeCommand(message = cliReader.readInput());
    }

    private void executeCommand(String message) {
        if (message.isEmpty()) return;
        if (message.startsWith("Log on") && !isRegistered) { // if a message times out, need to have a mechanism to keep track of that
            try {
                String helloRequest = "HELLO(" + username + ")";

                logger.info("Sending hello request to the server and waiting response...");
                String serverResponse = udpConnection.sendMessageAndGetResponse(helloRequest);

                String rand = Utils.extractValue(serverResponse);
                String res = Utils.createCipherKey(rand, password);
                Key encryptionKey = Utils.createEncryptionKey(res);
                String responseRequest = "RESPONSE(" + res + ")";
                udpConnection.addReceiverEncryption(encryptionKey);

                logger.info("Sending response request to the server and waiting response...");
                serverResponse = udpConnection.sendMessageAndGetResponse(responseRequest);

                if (serverResponse.equals("AUTH_SUCCESS")) {
                    logger.info("Authentication was successful");

                    Socket tcpSocket = new Socket(serverIpAddress, tcpPort);
                    logger.debug("Created tcp socket");

                    EncryptedTcpConnection encryptedTcpConnection = new EncryptedTcpConnection(tcpSocket, encryptionKey);
                    logger.debug("created encrypted tcp connection");

                    tcpMessagingService = new TcpService();
                    tcpMessagingService.setConnection(encryptedTcpConnection);
                    encryptedTcpConnection.disableEncryption();

                    logger.debug("Sending message with udp port...");
                    tcpMessagingService.queueMessage(String.valueOf(udpConnection.getPort()));

                    logger.debug("Starting tcp messaging service...");
                    tcpMessagingService.start();

                    logger.debug("Waiting for the port message to be transmitted...");
                    encryptedTcpConnection.enableReceiverEncryption();
                    while (!tcpMessagingService.areMessagesSent()) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    logger.debug("Port message has been transmitted");
                    encryptedTcpConnection.enableEncryption();
                } else
                    logger.info("Authentication was unsuccessful");
                logger.info("Client is now registered");
            } catch (TimeoutException | NoSuchAlgorithmException | IOException e) {
                if (e.getMessage().equals("Server either refused to respond or is unreachable"))
                    System.out.println("Server is down");
                else
                    logger.error(e.getMessage(), e);
            }
        } else if (message.startsWith("Log on") && isRegistered) {
            System.out.println("You are already logged in");
        }
        if (isRegistered) {
            if (message.startsWith("Chat") && !isInChatSession()) { // has to wait before receiving response
                String[] values = message.split(" ");
                if (values.length < 2) {
                    System.out.println("No corespondent specified.");
                    return;
                }
                corespondent = message.split(" ")[1];
                tcpMessagingService.queueMessage("CONNECT(" + corespondent + ")");
                waitForTcpResponse();
            } else if (message.startsWith("Chat") && isInChatSession())
                System.out.println("Already in a chat session. Exit current session first.");

            if (message.startsWith("History")) {
                String[] values = message.split(" ");
                if (values.length < 2) {
                    System.out.println("No corespondent specified.");
                    return;
                }
                String historyCorespondent = message.split(" ")[1];
                tcpMessagingService.queueMessage("HISTORY_REQ(" + historyCorespondent + ")");
            }

            if (message.startsWith("End chat") && isInChatSession()) {
                tcpMessagingService.queueMessage("END_NOTIF(" + sessionId + ")");
                corespondent = null;
                sessionId = null;
            } else if (message.startsWith("End chat") && !isInChatSession()) {
                System.out.println("You are not in a chat session to end it.");
            }

            if (message.startsWith("Log off")) {
                isRegistered = false;
                corespondent = null;
                sessionId = null;
                tcpMessagingService.shutdown();
                System.out.println("Logged off.");
            }
        } else if (message.startsWith("Chat") || message.startsWith("History") || message.startsWith("End chat") || message.startsWith("Log off")) {
            System.out.println("Cannot perform command. Must be registered.");
        }

        if (message.startsWith("Exit"))
            shutdown();

        if (message.startsWith("help"))
            System.out.println("Here is a list of available commands:[Log on, Log off, Chat <userid>, History, End Chat, Exit");

        String[] commands = new String[]{"Exit", "help", "Log on", "Log off", "Chat", "History", "End chat"};
        boolean isCommand = false;
        for (String command : commands) {
            if (message.startsWith(command)) {
                isCommand = true;
                break;
            }
        }
        if (!isCommand && isInChatSession() && isRegistered) {
            tcpMessagingService.queueMessage("CHAT(" + sessionId + "," + message + ")");
        }
    }

    private void waitForTcpResponse() {
        logger.info("Waiting for server to respond to tcp request");
        synchronized (tcpMessagingService) {
            try {
                tcpMessagingService.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.info("Server responded");
    }

    private void shutdown() {
        logger.info("Shutting down the chat client...");
        if (cliReader != null)
            cliReader.close();
        logger.info("Shutting down tcp messaging service...");
        if (tcpMessagingService != null)
            tcpMessagingService.shutdown();
        logger.info("Shutting down udp connection...");
        if (udpConnection != null)
            udpConnection.close();
        logger.info("Exiting...");
        System.exit(0);
    }

    private boolean isInChatSession() {
        return sessionId != null;
    }

    private class TcpService extends MessagingService {

        TcpService() {
            super();
        }

        @Override
        protected void processNextMessage() throws Exception {
            try {
                String message = retrieveMessage();
                if (message.startsWith("REGISTERED")) {
                    System.out.println("Online");
                    isRegistered = true;
                } else if (message.startsWith("START")) {
                    List<String> values = Utils.extractValues(message);
                    sessionId = values.get(0);
                    corespondent = values.get(1);
                    System.out.println("Chat started");
                } else if (message.startsWith("UNREACHABLE")) {
                    corespondent = null;
                    System.out.println("Correspondent unreachable");
                } else if (message.startsWith("END_NOTIF")) {
                    sessionId = null;
                    corespondent = null;
                    System.out.println("Chat ended");
                } else if (message.startsWith("CHAT") || message.startsWith("HISTORY_RESP")) {
                    List<String> values = Utils.extractValues(message);
                    System.out.println(values.get(1));
                }
            } catch (Exception e) {
                if (e.getMessage().equals("Connection reset")) {
                    sessionId = null;
                    corespondent = null;
                    isRegistered = false;
                }
                throw new Exception(e.getMessage(), e);
            } finally {
                synchronized (this) {
                    notifyAll();
                }
            }
        }
    }

    public static void main(String[] args) {
        ChatClientArguments chatClientArguments = new ChatClientArguments();
        JCommander jCommander = new JCommander(chatClientArguments);
        try {
            jCommander.parse(args);
        } catch (Exception ignored) {
            jCommander.usage();
            System.exit(0);
        }
        try {
            ChatClient chatClient = new ChatClient(chatClientArguments);
            chatClient.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
