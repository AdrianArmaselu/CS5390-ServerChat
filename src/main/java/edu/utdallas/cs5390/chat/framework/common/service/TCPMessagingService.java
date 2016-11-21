package edu.utdallas.cs5390.chat.framework.common.service;

import edu.utdallas.cs5390.chat.framework.common.ContextValues;
import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.connection.EncryptedTCPConnection;
import edu.utdallas.cs5390.chat.framework.common.util.Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by adisor on 10/30/2016.
 */

// TODO: THIS CLASS NEEDS A LOT OF REFACTORING
public class TCPMessagingService extends Thread {
    private EncryptedTCPConnection tcpConnection;
    private BlockingQueue<String> outgoingMessages;
    private Map<String, ContextualProtocol> protocols;
    private boolean stop;

    public TCPMessagingService() {
        protocols = new HashMap<>();
    }

    public TCPMessagingService(Socket clientSocket, Key encryptionKey) throws IOException {
        tcpConnection = new EncryptedTCPConnection(clientSocket, encryptionKey);
        outgoingMessages = new LinkedBlockingQueue<>();
        protocols = new HashMap<>();
    }

    public TCPMessagingService(EncryptedTCPConnection encryptedTCPConnection) throws IOException {
        tcpConnection = encryptedTCPConnection;
        outgoingMessages = new LinkedBlockingQueue<>();
        protocols = new HashMap<>();
    }

    public void setup(Socket clientSocket, Key encryptionKey) throws IOException {
        tcpConnection = new EncryptedTCPConnection(clientSocket, encryptionKey);
        outgoingMessages = new LinkedBlockingQueue<>();
        protocols = new HashMap<>();
    }

    public void addProtocol(String serverResponse, ContextualProtocol protocol) {
        protocols.put(serverResponse, protocol);
    }

    public void run() {
        while (!isInterrupted()) {
            sendMessages();
            processIncomingMessages();
        }
        stop = true;
    }

    public void queueMessage(String message) {
        outgoingMessages.add(message);
    }

    private void sendMessages() {
        while (!outgoingMessages.isEmpty())
            sendMessage();
    }

    void sendMessage() {
        try {
            tcpConnection.sendMessage(outgoingMessages.poll());
        } catch (IOException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void processIncomingMessages() {
        while (processNextMessage() != null) ;
    }

    private String processNextMessage() {
        String message = null;
        try {
            message = tcpConnection.receiveMessage();
            boolean isProtocolMessage = message.contains("(") && protocols.containsKey(message.substring(0, message.indexOf("(")));
            if (isProtocolMessage) {
                String protocolHeader = Utils.extractProtocolHeader(message);
                ContextualProtocol contextualProtocol = protocols.get(protocolHeader);
                contextualProtocol.setContextValue(ContextValues.message, message);
                contextualProtocol.executeProtocol();
            }
            else
                System.out.println(message);
        } catch (IOException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return message;
    }

    public void shutdown() {
        interrupt();
        while (!stop) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
