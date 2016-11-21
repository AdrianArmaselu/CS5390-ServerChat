package edu.utdallas.cs5390.chat.impl.client;

/**
 * Created by adisor on 10/30/2016.
 */
class ProtocolServerResponses {

    static final String END_NOTIF = "END_NOTIF";
    static final String HISTORY_RESP = "HISTORY_RESP";
    static final String START = "START";

    public static boolean isStartMessage(String message) {
        return message.startsWith("START");
    }

    static String extractValue(String message) {
        return message.substring(message.indexOf("(") + 1, message.lastIndexOf(")"));
    }

    static boolean isAuthSuccessful(String response) {
        return extractValue(response).equals("AUTH_SUCCESS");
    }

    static boolean isRegistered(String response){
        return response.startsWith("REGISTERED");
    }
}
