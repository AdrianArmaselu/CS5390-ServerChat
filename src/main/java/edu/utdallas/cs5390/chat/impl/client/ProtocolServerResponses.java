package edu.utdallas.cs5390.chat.impl.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    static List<String> extractValues(String message) {
        List<String> values = new ArrayList<>();
        String valuesString = message.substring(message.indexOf("(") + 1, message.lastIndexOf(")"));
        Collections.addAll(values, valuesString.split(","));
        return values;
    }

    static boolean isAuthSuccessful(String response) {
        return response.equals("AUTH_SUCCESS");
    }

    static boolean isRegistered(String response){
        return response.startsWith("REGISTERED");
    }
}
