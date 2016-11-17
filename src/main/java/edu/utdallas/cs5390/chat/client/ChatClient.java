package edu.utdallas.cs5390.chat.client;

import com.beust.jcommander.JCommander;
import edu.utdallas.cs5390.chat.client.protocol.CommandsProtocol;
import edu.utdallas.cs5390.chat.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.common.connection.udp.UDPConnection;
import edu.utdallas.cs5390.chat.common.service.TCPMessagingService;
import edu.utdallas.cs5390.chat.common.util.CLIReader;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.Key;

/**
 * Created by Adisor on 10/1/2016.
 */

// TODO: COMMUNICATE AVAILABLE COMMANDS BACK TO THE USER
public class ChatClient implements AbstractChatClient {
    private ChatClientArguments chatClientArguments;
    private UDPConnection udpConnection;
    private CLIReader cliReader;
    private final CommandsProtocol commandsProtocol;
    private TCPMessagingService tcpMessagingService;

    private boolean isInChatSession;

    public ChatClient(ChatClientArguments chatClientArguments) throws IOException {
        tcpMessagingService = new TCPMessagingService();
        this.chatClientArguments = chatClientArguments;
        commandsProtocol = new CommandsProtocol(this);
        cliReader = new CLIReader();
        try {
            udpConnection = new UDPConnection(chatClientArguments.getServerAddress(), chatClientArguments.getPort());
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    private void run() {
        String message = "";
        while (!message.equals("exit")) {
            message = cliReader.readInput();
            executeCommand(message);
        }
        shutdown();
    }

    private void executeCommand(String message) {
        commandsProtocol.setContextValue("cli.message", message);
        commandsProtocol.executeProtocol();
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

    public static void main(String[] args) {
        ChatClientArguments chatClientArguments = new ChatClientArguments();
        JCommander jCommander = new JCommander(chatClientArguments);
        try {
            jCommander.parse(args);
            ChatClient chatClient = new ChatClient(chatClientArguments);
            chatClient.run();
        } catch (Exception ignored) {
            jCommander.usage();
        }
    }
}
