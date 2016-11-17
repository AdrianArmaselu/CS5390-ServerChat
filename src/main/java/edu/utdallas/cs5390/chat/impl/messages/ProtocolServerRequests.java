package edu.utdallas.cs5390.chat.impl.messages;

/**
 * Created by adisor on 10/30/2016.
 */
public class ProtocolServerRequests {
    public static String HELLO(String username){
        return "HELLO(" + username + ")";
    }
    public static String RESPONSE(String cipherKey){return "RESPONSE(" + cipherKey + ")";}
    public static String REGISTER(String clientAddress){return "REGISTER("+clientAddress + ")";}

    public static String HISTORY_REQ(String partnerUsername) {
        return "HISTORY_REQ(" + partnerUsername + ")";
    }
}
