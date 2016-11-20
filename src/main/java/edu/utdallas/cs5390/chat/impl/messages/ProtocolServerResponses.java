package edu.utdallas.cs5390.chat.impl.messages;

/**
 * Created by adisor on 10/30/2016.
 */
public class ProtocolServerResponses {

    public static final String END_NOTIF = "END_NOTIF";
    public static final String HISTORY_RESP = "HISTORY_RESP";
    public static final String START = "START";

    public static boolean isStartMessage(String message) {
        return message.startsWith("START");
    }

    public static String extractValue(String message) {
        return message.substring(message.indexOf("(") + 1, message.lastIndexOf(")"));
    }

    public static boolean isAuthSuccessful(String response) {
        return extractValue(response).equals("AUTH_SUCCESS");
    }

    public static boolean isOK(String response){
        return response.equals("ok");
    }
}
