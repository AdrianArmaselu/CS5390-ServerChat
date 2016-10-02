package edu.utdallas.cs5390.chat.server;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Adisor on 10/1/2016.
 */
public class ClientProfile {
    String username;
    String password;
    boolean isLoggedIn;
    String sessionPartnerUsername;
    Queue<String> messages;
    public ClientService clientService;

    public ClientProfile() {
        messages = new LinkedList<String>();
    }
}
