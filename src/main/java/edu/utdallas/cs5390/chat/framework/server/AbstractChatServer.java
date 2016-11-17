package edu.utdallas.cs5390.chat.framework.server;

import java.security.Key;

/**
 * Created by aarmaselu on 10/11/2016.
 */
public interface AbstractChatServer {
    boolean isASubscriber(String username);

    String getUserSecretKey(String username);

    void setId(String username, String address);

    String getUsername(String ipAddress);

    boolean hasMatchingRes(String username, String cipherKey);

    String getRand(String username);

    void saveRand(String username, String rand);

    Key generateEncryptionKey(String username);

    void acceptTCPConnectionFromUser(String username);

    Key getUserEncryptionKey(String ipAddress);
}
