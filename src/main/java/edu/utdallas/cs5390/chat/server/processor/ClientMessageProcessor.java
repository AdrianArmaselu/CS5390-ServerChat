package edu.utdallas.cs5390.chat.server.processor;

import edu.utdallas.cs5390.chat.server.AbstractChatServer;

/**
 * Created by Adisor on 10/1/2016.
 */
public class ClientMessageProcessor implements MessageProcessor {

    private AbstractChatServer chatServer;

    public ClientMessageProcessor(AbstractChatServer chatServer) {
        this.chatServer = chatServer;
    }

    @Override
    public void processMessage(String message) {
        //first check that the message is not a command
//        if(notIsCommand(message)){
//            AbstractClientMessagingService abstractClientMessagingService = chatServer.getAbstractClientMessagingService();
//            String userId = extractId(message);
//            String userId = chatServer.getPartnerId();
//            abstractClientMessagingService.sendMessage(message, "partnerID");
//        }
        if (isEndSession(message)) {
            chatServer.endSession("userID");
        } else if (showChatHistory(message)) {
        }
    }

    private String extractId(String message) {
        return "implementation";
    }

    private boolean notIsCommand(String message) {
        return false;
    }
}
