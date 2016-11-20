package edu.utdallas.cs5390.chat.framework.client;

import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.connection.udp.UDPConnection;

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

    void addTCPProtocol(String serverResponse, ContextualProtocol responseProtocol);

    void startTCPMessagingService(String serverAddress, int serverPort, Key secretKey);

    void run();

    void addCliProtocol(String command, ContextualProtocol commandsProtocol);

    String getPartnerUsername();

    void setPartnerUsername(String partnerUsername);
}
