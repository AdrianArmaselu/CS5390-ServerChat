package edu.utdallas.cs5390.chat.client.protocol;

import edu.utdallas.cs5390.chat.client.AbstractChatClient;
import edu.utdallas.cs5390.chat.client.connection.EncryptedTCPClientConnection;

/**
 * Created by adisor on 10/30/2016.
 */
public class StartChatProtocol implements ClientProtocol {
    private String chatPartnerUsername;

    public void setChatPartnerUsername(String chatPartnerUsername) {
        this.chatPartnerUsername = chatPartnerUsername;
    }

    @Override
    public void executeProtocol(AbstractChatClient chatClient) throws Exception {
        EncryptedTCPClientConnection encryptedTcpClientConnection = chatClient.getTcpConnection();
        String response = encryptedTcpClientConnection.sendMessageAndGetResponse("connect: " + chatPartnerUsername);
        System.out.println(response.equals("start") ? "Chat started" : "User unreachable");
    }
}
