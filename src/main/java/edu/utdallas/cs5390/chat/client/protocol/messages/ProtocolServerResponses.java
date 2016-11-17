package edu.utdallas.cs5390.chat.client.protocol.messages;

/**
 * Created by adisor on 10/30/2016.
 */
public class ProtocolServerResponses {

    public static final String CHAT_ENDED = "chatended";

    public static boolean isStartMessage(String message) {
        return message.startsWith("START");
    }
}
