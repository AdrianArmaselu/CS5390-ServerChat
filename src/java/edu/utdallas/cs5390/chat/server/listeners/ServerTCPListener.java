package edu.utdallas.cs5390.chat.server.listeners;

import edu.utdallas.cs5390.chat.server.ChatServer;
import edu.utdallas.cs5390.chat.server.ClientProfile;
import edu.utdallas.cs5390.chat.server.ClientService;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Adisor on 10/1/2016.
 */
public class ServerTCPListener implements MessageListener {

    private ChatServer chatServer;

    public ServerTCPListener(ChatServer chatServer) {
        this.chatServer = chatServer;
    }

    @Override
    public void processMessage(ClientService clientService, String message) {
        if (message.equals("lala")) {
            try {
                clientService.getPartner().sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(message.equals("startChat")){
            Map<String, ClientProfile> clientProfileMap = chatServer.getClientProfileMap();
            ClientService partnerService = clientProfileMap.get("passPartnerUsernameHere").clientService;
            clientService.setPartner(partnerService);
        }

        // somewhere here the server should remove the client service if it disconnects
    }
}
