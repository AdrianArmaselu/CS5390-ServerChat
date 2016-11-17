package edu.utdallas.cs5390.chat.framework.server.protocol;

/**
 * Created by adisor on 10/30/2016.
 */
public class ProtocolIncomingMessages {

    public static boolean isHelloMessage(String message) {
        return message.startsWith("HELLO");
    }

    public static boolean isResponseMessage(String message) {
        return message.startsWith("RESPONSE");
    }

    public static boolean isRegisteredMessage(String message) {
        return message.startsWith("REGISTER");
    }

    public static boolean isConnectMessage(String message) {
        return message.startsWith("CONNECT");
    }

    public static String extractUsername(String message) {
        return extractValue(message);
    }

    public static String extractRes(String message) {
        return extractValue(message);
    }

    private static String extractValue(String message) {
        return message.substring(message.indexOf("(") + 1, message.lastIndexOf(")"));
    }
}