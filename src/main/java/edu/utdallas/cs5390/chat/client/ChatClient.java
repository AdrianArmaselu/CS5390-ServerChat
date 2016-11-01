package edu.utdallas.cs5390.chat.client;

import edu.utdallas.cs5390.chat.CLIReader;
import edu.utdallas.cs5390.chat.client.protocol.messages.ProtocolInputCommands;
import edu.utdallas.cs5390.chat.client.connection.EncryptedTCPClientConnection;
import edu.utdallas.cs5390.chat.client.connection.UDPClientConnection;
import edu.utdallas.cs5390.chat.client.protocol.ClientProtocol;
import edu.utdallas.cs5390.chat.client.protocol.EndChatProtocol;
import edu.utdallas.cs5390.chat.client.protocol.LogonProtocol;
import edu.utdallas.cs5390.chat.client.protocol.StartChatProtocol;

import java.net.SocketException;

/**
 * Created by Adisor on 10/1/2016.
 */

// TODO: COMMUNICATE AVAILABLE COMMANDS BACK TO THE USER
// TODO: PUT THE CLI CODE IN A DISPATCHER CLASS

public class ChatClient implements AbstractChatClient {
    // target host
    private static final String HOST_ADDRESS = "127.0.0.1";
    private static final int HOST_PORT = 8080;
    private UDPClientConnection udpConnection;
    private EncryptedTCPClientConnection tcpConnection;
    private CLIReader cliReader;

    private String username = "student";
    private String password = "student";

    private ClientProtocol logonProtocol;
    private ClientProtocol startChatProtocol;
    private ClientProtocol endChatProtocol;

    private ClientMessagingService clientMessagingService;

    private boolean isChatStarted;

    public ChatClient() {
        logonProtocol = new LogonProtocol();
        startChatProtocol = new StartChatProtocol();
        endChatProtocol = new EndChatProtocol();
        clientMessagingService = new ClientMessagingService(this);
        clientMessagingService.start();
        try {
            udpConnection = new UDPClientConnection(HOST_ADDRESS, HOST_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        cliReader = new CLIReader();
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
        switch (message) {
            case ProtocolInputCommands.LOGON:
                logon();
                break;
            case ProtocolInputCommands.START_CHAT:
                startChat();
                break;
            case ProtocolInputCommands.END_CHAT:
                endChat();
                break;
            case ProtocolInputCommands.LOGOFF:
                logoff();
                break;
            case ProtocolInputCommands.EXIT:
                shutdown();
                break;
            default:
                if (isChatStarted)
                    clientMessagingService.addMessage(message);
                else
                    System.out.println("Command Not Recognized. Here is a list of available commands: <Needs development>");
        }
    }

    private void shutdown() {
        clientMessagingService.shutdown();
        udpConnection.close();
        tcpConnection.close();
        cliReader.close();
        System.exit(0);
    }

    private void logon() {
        try {
            logonProtocol.executeProtocol(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startChat() {
        String chatPartnerUsername = cliReader.readInput();
        try {
            ((StartChatProtocol) startChatProtocol).setChatPartnerUsername(chatPartnerUsername);
            startChatProtocol.executeProtocol(this);
            isChatStarted = true;
        } catch (Exception e) {
            e.printStackTrace();
            isChatStarted = false;
        }
    }

    private void endChat() {
        try {
            endChatProtocol.executeProtocol(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logoff() {
        tcpConnection.close();
    }

    public UDPClientConnection getUdpConnection() {
        return udpConnection;
    }

    public void setTcpConnection(EncryptedTCPClientConnection tcpConnection) {
        this.tcpConnection = tcpConnection;
    }

    @Override
    public EncryptedTCPClientConnection getTcpConnection() {
        return tcpConnection;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getHost() {
        return HOST_ADDRESS;
    }

    @Override
    public int getPort() {
        return HOST_PORT;
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.run();
    }
}
