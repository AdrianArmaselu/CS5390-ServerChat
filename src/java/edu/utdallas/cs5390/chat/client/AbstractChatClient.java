package edu.utdallas.cs5390.chat.client;

import edu.utdallas.cs5390.chat.client.connection.EncryptedTCPClientConnection;
import edu.utdallas.cs5390.chat.client.connection.UDPClientConnection;

/**
 * Created by adisor on 10/30/2016.
 */
public interface AbstractChatClient {
    UDPClientConnection getUdpConnection();

    String getUsername();

    String getPassword();

    String getHost();

    int getPort();

    void setTcpConnection(EncryptedTCPClientConnection encryptedTcpClientConnection);

    EncryptedTCPClientConnection getTcpConnection();

}
