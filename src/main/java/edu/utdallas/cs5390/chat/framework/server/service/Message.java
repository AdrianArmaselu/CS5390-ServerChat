package edu.utdallas.cs5390.chat.framework.server.service;

public class Message {
    int sessionID;
    private String fromID;
    private String toID;
    private String content;


    Message(int sessionID, String fromID, String toID, String content) {
        this.sessionID = sessionID;
        this.fromID = fromID;
        this.toID = toID;
        this.content = content;
    }

    Message() {
        sessionID = 0;
        fromID = "";
        toID = "";
        content = "";
    }

    public int getSessionID() {
        return sessionID;
    }

    public void setSessionID(int id) {
        sessionID = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String newContent) {
        content = newContent;
    }

    public String getFromID() {
        return fromID;
    }

    public void setFromID(String newFromID) {
        fromID = newFromID;
    }

    public String getToID() {
        return toID;
    }

    public void setToID(String newToID) {
        toID = newToID;
    }
}