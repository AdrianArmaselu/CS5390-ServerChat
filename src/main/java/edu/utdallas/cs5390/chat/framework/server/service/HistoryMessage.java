package edu.utdallas.cs5390.chat.framework.server.service;

public class HistoryMessage {
    String sessionID;
    private String fromID;
    private String toID;
    private String content;


    public HistoryMessage(String sessionID, String fromID, String toID, String content) {
        this.sessionID = sessionID;
        this.fromID = fromID;
        this.toID = toID;
        this.content = content;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String id) {
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