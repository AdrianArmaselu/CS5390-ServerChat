package edu.utdallas.cs5390.chat.server.listeners;

import edu.utdallas.cs5390.chat.server.ClientService;

/**
 * Created by Adisor on 10/1/2016.
 */
public interface MessageListener {
    void processMessage(ClientService clientService, String message);
}
