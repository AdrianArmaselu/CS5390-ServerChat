package edu.utdallas.cs5390.chat.server.service;

/**
 * Created by aarmaselu on 10/11/2016.
 */
public interface AbstractClientMessagingService {
    void sendMessage(String message, String partnerID);
}
