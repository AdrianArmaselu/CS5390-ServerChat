package edu.utdallas.cs5390.chat.impl.server;

/**
 * Created by adisor on 10/30/2016.
 */
class ProtocolOutgoingMessages {

    static final String AUTH_SUCCESS = "AUTH_SUCCESS";

    static final String AUTH_FAIL = "AUTH_FAIL";

    static String CHALLENGE(String code) {
        return "CHALLENGE (" + code + ")";
    }

    static String START(String sessionId, String username) {
        return "START(" + sessionId + "," + username + ")";
    }

    static String UNREACHABLE(String username) {
        return "UNREACHABLE(" + username + ")";
    }

    static String END_NOTIF(String sessionId) {
        return "END_NOTIF(" + sessionId + ")";
    }
}
