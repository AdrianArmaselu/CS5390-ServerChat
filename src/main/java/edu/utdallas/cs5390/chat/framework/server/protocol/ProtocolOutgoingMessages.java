package edu.utdallas.cs5390.chat.framework.server.protocol;

/**
 * Created by adisor on 10/30/2016.
 */
public class ProtocolOutgoingMessages {

    public static String CHALLENGE(String code) {
        return "Challenge (" + code + ")";
    }

    public static final String AUTH_SUCCESS = "AUTH_SUCCESS";

    public static final String AUTH_FAIL = "AUTH_FAIL";
}
