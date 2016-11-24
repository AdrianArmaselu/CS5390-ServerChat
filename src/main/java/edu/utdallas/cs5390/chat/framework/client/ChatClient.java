package edu.utdallas.cs5390.chat.framework.client;

import edu.utdallas.cs5390.chat.framework.common.ContextValues;
import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.connection.UdpConnection;
import edu.utdallas.cs5390.chat.framework.common.service.TCPMessagingService;
import edu.utdallas.cs5390.chat.framework.common.util.CLIReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Adisor on 10/1/2016.
 */

// TODO: COMMUNICATE AVAILABLE COMMANDS BACK TO THE USER
public class ChatClient implements AbstractChatClient {
    private final Logger logger = LoggerFactory.getLogger(ChatClient.class);

    private ChatClientArguments chatClientArguments;
    private UdpConnection udpConnection;
    private CLIReader cliReader;
    private Map<String, ContextualProtocol> cliProtocols;
    private TCPMessagingService tcpMessagingService;
    private String partnerUsername;
    private String sessionId;

    public ChatClient(ChatClientArguments chatClientArguments) throws IOException {
        tcpMessagingService = new TCPMessagingService();
        tcpMessagingService.setOnChatMessageProtocol(new ContextualProtocol() {
            @Override
            public void executeProtocol() {
                System.out.println(getContextValue(ContextValues.message));
            }
        });
        cliProtocols = new HashMap<>();
        this.chatClientArguments = chatClientArguments;
        cliReader = new CLIReader();
        boolean isRunning = true;
        int port = 9000;
        do
            try {
                DatagramSocket datagramSocket = new DatagramSocket(port++);
                datagramSocket.setSoTimeout(1000);
                udpConnection = new UdpConnection(chatClientArguments.getServerAddress(), chatClientArguments.getUdpServerPort());
                isRunning = true;
            } catch (SocketException | UnknownHostException e) {
                if (e.getMessage().equals("Address already in use: Cannot bind")) {
                    isRunning = false;
                }else{
                    e.printStackTrace();
                }
            }
        while (!isRunning);
    }

    public void run() {
        logger.info("started client");
        String message = "";
        while (!message.equals("exit")) {
            message = cliReader.readInput();
            executeCommand(message);
        }
        shutdown();
    }

    @Override
    public void addCliProtocol(String command, ContextualProtocol cliProtocol) {
        cliProtocols.put(command, cliProtocol);
    }

    @Override
    public String getPartnerUsername() {
        return partnerUsername;
    }

    @Override
    public void setPartnerUsername(String partnerUsername) {
        this.partnerUsername = partnerUsername;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    private void executeCommand(String message) {
        String[] messageWords = message.split(" ");
        boolean hasProtocolForMessageFirstWord = cliProtocols.containsKey(messageWords[0]);
        if(hasProtocolForMessageFirstWord && messageWords.length > 1){
            ContextualProtocol protocol = cliProtocols.get(messageWords[0]);
            protocol.setContextValue(ContextValues.chatPartnerUsername, messageWords[1]);
            protocol.executeProtocol();
        }
        else if (cliProtocols.containsKey(message))
            cliProtocols.get(message).executeProtocol();
        else if (!cliProtocols.containsKey(message) && isInChatSession())
            queueMessage(message);
        else if (!cliProtocols.containsKey(message) && !isInChatSession())
            System.out.println("Command Not Recognized. Here is a list of available commands:[Log on, Log off, Chat <userid>, History, End Chat, Exit");
    }

    public void shutdown() {
        if (tcpMessagingService != null)
            tcpMessagingService.shutdown();
        if (udpConnection != null)
            udpConnection.close();
        if (cliReader != null)
            cliReader.close();
        System.exit(0);
    }

    @Override
    public void addTCPProtocol(String serverResponse, ContextualProtocol responseProtocol) {
        tcpMessagingService.addProtocol(serverResponse, responseProtocol);
    }

    @Override
    public void startTCPMessagingService(Key secretKey) {
        try {
            logger.info("Started tcp messaging service");
            tcpMessagingService.setup(new Socket(chatClientArguments.getServerAddress(), chatClientArguments.getTcpPort()), secretKey);
            tcpMessagingService.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UdpConnection getUDPConnection() {
        return udpConnection;
    }

    @Override
    public boolean isInChatSession() {
        return sessionId != null;
    }

    @Override
    public void queueMessage(String message) {
        tcpMessagingService.queueMessage(message);
    }

    @Override
    public void logoff() {
        tcpMessagingService.shutdown();
    }

    public String getUsername() {
        return chatClientArguments.getUsername();
    }

    public String getPassword() {
        return chatClientArguments.getPassword();
    }
}
