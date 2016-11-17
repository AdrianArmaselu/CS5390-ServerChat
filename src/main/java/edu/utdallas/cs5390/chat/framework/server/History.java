package edu.utdallas.cs5390.chat.framework.server;

import java.util.*;

public class History {
    private LinkedList<Message> chatHistory = new LinkedList();

    History() {
    }

    public void storeMessage(Message message) {
        chatHistory.add(message);
    }

    public History getHistory(String clientID1, String clientID2) {
        History userHistory = new History();
        for (Message chatMessage : chatHistory) {
            if ((chatMessage.getFromID().equals(clientID1) || chatMessage.getFromID().equals(clientID2)) && (chatMessage.getToID().equals(clientID1) || chatMessage.getToID().equals(clientID2))) {
                userHistory.storeMessage(chatMessage);
            }
        }
        return userHistory;
    }

    public void showHistory() {
        System.out.println();
        if (chatHistory.isEmpty()) {
            System.out.println("No previous chat history to display.");
            System.out.println();
        } else {
            for (Message chatMessage : chatHistory) {
                System.out.println(chatMessage.getSessionID() + "\t From: " + chatMessage.getFromID() + "\t " + chatMessage.getContent());
            }
        }
    }
}

