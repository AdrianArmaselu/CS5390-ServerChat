package edu.utdallas.cs5390.chat.framework.server;

import edu.utdallas.cs5390.chat.framework.server.service.ClientProfile;

import java.net.Socket;

/**
 * Created by aarmaselu on 10/11/2016.
 */
public interface AbstractChatServer {
    boolean isASubscriber(String username);

    ClientProfile getProfileByUsername(String username);

    ClientProfile getProfileByIp(String ipAddress);

    void addIpProfile(String ipAddress, ClientProfile usernameProfile);

    void startSession(Socket clientSocket);
}
