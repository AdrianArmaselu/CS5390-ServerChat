package edu.utdallas.cs5390.chat.framework.common.service;

import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.connection.EncryptedTCPConnection;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by adisor on 10/30/2016.
 */

// TODO: THIS CLASS NEEDS A LOT OF REFACTORING
public class TCPMessagingService extends Thread {
    private EncryptedTCPConnection tcpConnection;
    private BlockingQueue<String> outgoingMessages;
    private Map<String,ContextualProtocol> protocols;
    private boolean stop;

    public TCPMessagingService(){
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

    // used for received message porocessing
    public void addCustomProtocol(String command, ContextualProtocol protocol){
        protocols.put(command, protocol);
    }

    public void run() {
        while (!isInterrupted()) {
            sendMessages();
            printReceivedMessages();
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

    private void printReceivedMessages() {
        while (executeProtocols() != null) ;
    }

    private String executeProtocols() {
        String receivedMessage = null;
        try {
            receivedMessage = tcpConnection.receiveMessage();
            if(receivedMessage != null)
                protocols.get(receivedMessage).executeProtocol();
            System.out.println(receivedMessage);
        } catch (IOException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return receivedMessage;
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
