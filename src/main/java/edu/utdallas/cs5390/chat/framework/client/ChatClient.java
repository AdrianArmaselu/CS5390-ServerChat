package edu.utdallas.cs5390.chat.framework.client;

import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.connection.udp.UDPConnection;
import edu.utdallas.cs5390.chat.framework.common.service.TCPMessagingService;
import edu.utdallas.cs5390.chat.framework.common.util.CLIReader;

import java.io.IOException;
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
    private ChatClientArguments chatClientArguments;
    private UDPConnection udpConnection;
    private CLIReader cliReader;
    private Map<String, ContextualProtocol> cliProtocols;
    private TCPMessagingService tcpMessagingService;
    private String partnerUsername;
    private boolean isInChatSession;

    public ChatClient(ChatClientArguments chatClientArguments) throws IOException {
        tcpMessagingService = new TCPMessagingService();
        cliProtocols = new HashMap<>();
        this.chatClientArguments = chatClientArguments;
        cliReader = new CLIReader();
        try {
            udpConnection = new UDPConnection(chatClientArguments.getServerAddress(), chatClientArguments.getPort());
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    public void run() {
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

    private void executeCommand(String message) {
        cliProtocols.get(message).executeProtocol();
        // nullpointer exception
        if (!cliProtocols.containsKey(message) && isInChatSession())
            queueMessage(message);
        else if (!cliProtocols.containsKey(message) && !isInChatSession())
            System.out.println("Command Not Recognized. Here is a list of available commands: <Needs development>");
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
    public void addTCPProtocol(ContextualProtocol contextualProtocol) {
        tcpMessagingService.addCustomProtocol(contextualProtocol);
    }

    @Override
    public void startTCPMessagingService(String serverAddress, int serverPort, Key secretKey) {
        try {
            tcpMessagingService.setup(new Socket(serverAddress, serverPort), secretKey);
            tcpMessagingService.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UDPConnection getUDPConnection() {
        return udpConnection;
    }

    @Override
    public void setIsInChatSession(boolean isInChatSession) {
        this.isInChatSession = isInChatSession;
    }

    @Override
    public boolean isInChatSession() {
        return isInChatSession;
    }

    @Override
    public void queueMessage(String message) {
        tcpMessagingService.queueMessage(message);
    }

    @Override
    public String readInput() {
        return cliReader.readInput();
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

    @Override
    public String getServerAddress() {
        return chatClientArguments.getServerAddress();
    }

    @Override
    public int getServerPort() {
        return chatClientArguments.getPort();
    }
}
