package edu.utdallas.cs5390.chat.client.protocol;

import edu.utdallas.cs5390.chat.client.AbstractChatClient;
import edu.utdallas.cs5390.chat.client.protocol.messages.ProtocolServerResponses;
import edu.utdallas.cs5390.chat.client.connection.EncryptedTCPClientConnection;

/**
 * Created by adisor on 10/30/2016.
 */
public class EndChatProtocol implements ClientProtocol {
    @Override
    public void executeProtocol(AbstractChatClient chatClient) throws Exception {
        EncryptedTCPClientConnection encryptedTcpClientConnection = chatClient.getTcpConnection();
        String response = encryptedTcpClientConnection.sendMessageAndGetResponse("end");
        System.out.println(response.equals(ProtocolServerResponses.CHAT_ENDED) ? "Chat Ended" : "User unreachable");
    }
}
