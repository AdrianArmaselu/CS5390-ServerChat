package edu.utdallas.cs5390.chat.client;

import edu.utdallas.cs5390.chat.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.common.connection.udp.UDPConnection;

import java.security.Key;

/**
 * Created by adisor on 10/30/2016.
 */
public interface AbstractChatClient {
    UDPConnection getUDPConnection();

    String getUsername();

    String getPassword();

    String getServerAddress();

    int getServerPort();

    void setIsInChatSession(boolean isInChatSession);

    boolean isInChatSession();

    void queueMessage(String message);

    String readInput();

    void logoff();

    void shutdown();

    void addTCPProtocol(ContextualProtocol contextualProtocol);

    void startTCPMessagingService(String serverAddress, int serverPort, Key secretKey);
}
