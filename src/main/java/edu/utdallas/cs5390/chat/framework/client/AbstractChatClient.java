package edu.utdallas.cs5390.chat.framework.client;

import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.connection.UdpConnection;

import java.security.Key;

/**
 * Created by adisor on 10/30/2016.
 */
public interface AbstractChatClient {
    UdpConnection getUDPConnection();

    String getUsername();

    String getPassword();

    boolean isInChatSession();

    void queueMessage(String message);

    void logoff();

    void shutdown();

    void addTCPProtocol(String serverResponse, ContextualProtocol responseProtocol);

    void startTCPMessagingService(Key secretKey);

    void run();

    void addCliProtocol(String command, ContextualProtocol commandsProtocol);

    String getPartnerUsername();

    void setPartnerUsername(String partnerUsername);

    String getSessionId();

    void setSessionId(String sessionId);
}
