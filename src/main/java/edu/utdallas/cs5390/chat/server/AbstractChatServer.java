package edu.utdallas.cs5390.chat.server;

import java.net.InetAddress;
import java.security.Key;

/**
 * Created by aarmaselu on 10/11/2016.
 */
public interface AbstractChatServer {
    boolean isASubscriber(String username);

    String getUserSecretKey(String username);

    void setId(String username, String address);

    String getUsername(String ipAddress);

    boolean hasMatchingCipherKey(String username, String cipherKey);

    String getRand(String username);

    void saveRand(String username, String rand);

    Key generateEncryptionKey();

    void acceptTCPConnectionFromUser(String username);
}
