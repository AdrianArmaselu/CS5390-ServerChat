package edu.utdallas.cs5390.chat.framework.server;

import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.service.TCPMessagingService;
import edu.utdallas.cs5390.chat.framework.server.service.ClientProfile;

import java.net.Socket;

/**
 * Created by aarmaselu on 10/11/2016.
 */
public interface AbstractChatServer {
    boolean isASubscriber(String username);

    ClientProfile getProfileByUsername(String username);

    ClientProfile getProfileByAddress(String ipAddress);

    void addAddressProfile(String ipAddress, ClientProfile usernameProfile);

    void startSession(Socket clientSocket);

    void addTcpProtocol(String tcpMessage, ContextualProtocol tcpProtocol);

    void addUdpProtocol(String udpMessage, ContextualProtocol udpProtocol);

    TCPMessagingService getTcpMessagingService(String targetUsername);

    void startup();

    void shutdown();
}
