package edu.utdallas.cs5390.chat.impl.client;

/**
 * Created by adisor on 10/30/2016.
 */
class ProtocolServerRequests {
    static String HELLO(String username){
        return "HELLO(" + username + ")";
    }
    static String RESPONSE(String cipherKey){return "RESPONSE(" + cipherKey + ")";}
    static String REGISTER(String clientAddress){return "REGISTER("+clientAddress + ")";}
    static String HISTORY_REQ(String partnerUsername) {
        return "HISTORY_REQ(" + partnerUsername + ")";
    }
    static String CONNECT(String partnerUsername) {return "CONNECT(" + partnerUsername + ")";}
    static String END_REQUEST(String sessionId){return "END_REQUEST(" + sessionId + ")";}
}
