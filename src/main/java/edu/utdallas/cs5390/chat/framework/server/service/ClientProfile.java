package edu.utdallas.cs5390.chat.framework.server.service;

import edu.utdallas.cs5390.chat.framework.common.util.Utils;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Adisor on 10/1/2016.
 */
public class ClientProfile {
    public String username;
    public String password;
    public String rand;
    public boolean isRegistered;
    public String cipherKey;
    public Key encryptionKey;
    public List<Message> chatHistory;
    public boolean inSession;
    public String sessionId;

    public ClientProfile() {
    }

    public void createEncryptionKeys() throws NoSuchAlgorithmException {
        cipherKey = Utils.createCipherKey(rand, password);
        encryptionKey = Utils.createEncryptionKey(cipherKey);
    }

    public void startSession(){
        chatHistory = new LinkedList<>();
    }

    public void storeMessage(Message message) {
        chatHistory.add(message);
    }

    public String getHistory() {
        final StringBuilder stringBuilder = new StringBuilder();
        if (chatHistory.isEmpty())
            stringBuilder.append("No previous chat history to display.\n");
        chatHistory.forEach(message ->
                stringBuilder.append(message.getSessionID())
                        .append("\t From: ")
                        .append(message.getFromID())
                        .append("\t ")
                        .append(message.getContent())

        );
        return stringBuilder.toString();
    }
}
