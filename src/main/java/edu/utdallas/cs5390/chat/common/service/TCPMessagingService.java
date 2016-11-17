package edu.utdallas.cs5390.chat.common.service;

import edu.utdallas.cs5390.chat.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.common.connection.EncryptedTCPConnection;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by adisor on 10/30/2016.
 */

// TODO: THIS CLASS NEEDS A LOT OF REFACTORING
public class TCPMessagingService extends Thread {
    private EncryptedTCPConnection tcpConnection;
    private BlockingQueue<String> outgoingMessages;
    private List<ContextualProtocol> protocols;
    private boolean stop;

    public TCPMessagingService(){
        protocols = new ArrayList<>();
    }

    public TCPMessagingService(Socket clientSocket, Key encryptionKey) throws IOException {
        tcpConnection = new EncryptedTCPConnection(clientSocket, encryptionKey);
        outgoingMessages = new LinkedBlockingQueue<>();
        protocols = new LinkedList<>();
    }

    public TCPMessagingService(EncryptedTCPConnection encryptedTCPConnection) throws IOException {
        tcpConnection = encryptedTCPConnection;
        outgoingMessages = new LinkedBlockingQueue<>();
        protocols = new LinkedList<>();
    }

    public void setup(Socket clientSocket, Key encryptionKey) throws IOException {
        tcpConnection = new EncryptedTCPConnection(clientSocket, encryptionKey);
        outgoingMessages = new LinkedBlockingQueue<>();
        protocols = new LinkedList<>();
    }

    // used for received message porocessing
    public void addCustomProtocol(ContextualProtocol protocol){
        protocols.add(protocol);
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
            String finalReceivedMessage = receivedMessage;
            protocols.forEach(protocol -> {
                try {
                    protocol.setContextValue("key", "value");
                    protocol.executeProtocol();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
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
