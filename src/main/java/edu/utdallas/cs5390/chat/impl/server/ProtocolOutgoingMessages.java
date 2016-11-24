package edu.utdallas.cs5390.chat.impl.server;

/**
 * Created by adisor on 10/30/2016.
 */
public class ProtocolOutgoingMessages {

    public static final String AUTH_SUCCESS = "AUTH_SUCCESS";

    public static final String AUTH_FAIL = "AUTH_FAIL";
    public static final String REGISTERED = "REGISTERED";

    public static String CHALLENGE(String code) {
        return "CHALLENGE (" + code + ")";
    }

    public static String START(String sessionId, String username) {
        return "START(" + sessionId + "," + username + ")";
    }

    public static String UNREACHABLE(String username) {
        return "UNREACHABLE(" + username + ")";
    }

    public static String END_NOTIF(String sessionId) {
        return "END_NOTIF(" + sessionId + ")";
    }
}
