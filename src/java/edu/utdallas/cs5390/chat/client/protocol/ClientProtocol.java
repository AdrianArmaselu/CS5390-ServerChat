package edu.utdallas.cs5390.chat.client.protocol;

import edu.utdallas.cs5390.chat.client.AbstractChatClient;

/**
 * Created by adisor on 10/30/2016.
 * This is used to isolate the communication logic from the client side
 */
public interface ClientProtocol {
    public void executeProtocol(AbstractChatClient chatClient) throws Exception;
}
