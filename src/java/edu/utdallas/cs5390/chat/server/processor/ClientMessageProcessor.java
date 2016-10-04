package edu.utdallas.cs5390.chat.server.processor;

import edu.utdallas.cs5390.chat.server.ChatServer;

/**
 * Created by Adisor on 10/1/2016.
 */
public class ClientMessageProcessor implements MessageProcessor {

    private ChatServer chatServer;

    public ClientMessageProcessor(ChatServer chatServer) {
        this.chatServer = chatServer;
    }

    @Override
    public void processMessage(String message) {
    }
}
