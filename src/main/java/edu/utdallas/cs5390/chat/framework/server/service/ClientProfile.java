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
    public boolean isRegistered;
    public String xRes;
    public Key encryptionKey;
    public List<HistoryMessage> chatHistory;
    public boolean inSession;
    public String sessionId;

    public ClientProfile() {
        chatHistory = new LinkedList<>();
    }

    public void storeMessage(HistoryMessage historyMessage) {
        chatHistory.add(historyMessage);
    }

    public String getHistory() {
        final StringBuilder stringBuilder = new StringBuilder();
        if (chatHistory.isEmpty())
            stringBuilder.append("No previous chat history to display.\n");
        chatHistory.forEach(historyMessage ->
                stringBuilder.append(historyMessage.getSessionID())
                        .append("\t From: ")
                        .append(historyMessage.getFromID())
                        .append("\t ")
                        .append(historyMessage.getContent())
                        .append("\n")

        );
        return stringBuilder.toString();
    }

    public void createXRes(String rand) throws NoSuchAlgorithmException {
        xRes = Utils.createCipherKey(rand, password);
    }

    public void createEncryptionKey() {
        encryptionKey = Utils.createEncryptionKey(xRes);
    }

    public void reset() {
        isRegistered = false;
        xRes = null;
        encryptionKey = null;
        chatHistory = null;
        inSession = false;
        sessionId = null;
    }
}
