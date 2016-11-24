package edu.utdallas.cs5390.chat.framework.common.service;

import edu.utdallas.cs5390.chat.framework.common.ContextValues;
import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.EncryptedTcpConnection;
import edu.utdallas.cs5390.chat.framework.common.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
//TODO: IN SOME SITUATIONS, NEED TO ENSURE DELIVERY OF MESSAGES
public class TCPMessagingService extends Thread {
    private final Logger logger = LoggerFactory.getLogger(TCPMessagingService.class);
    private EncryptedTcpConnection encryptedTcpConnection;
    private BlockingQueue<String> outgoingMessages;
    private Map<String, ContextualProtocol> protocols;
    private ContextualProtocol onChatMessageProtocol;
    private boolean stop;
    private boolean hasStopped;
    public String username;
    public String partner;

    public TCPMessagingService() {
        protocols = new HashMap<>();
    }

    public TCPMessagingService(Socket clientSocket, Key encryptionKey) throws IOException {
        encryptedTcpConnection = new EncryptedTcpConnection(clientSocket, encryptionKey);
        outgoingMessages = new LinkedBlockingQueue<>();
        protocols = new HashMap<>();
    }

    public void setOnChatMessageProtocol(ContextualProtocol onChatMessageProtocol) {
        this.onChatMessageProtocol = onChatMessageProtocol;
    }

    public void setup(Socket clientSocket, Key encryptionKey) throws IOException {
        encryptedTcpConnection = new EncryptedTcpConnection(clientSocket, encryptionKey);
        outgoingMessages = new LinkedBlockingQueue<>();
    }

    public void addProtocol(String message, ContextualProtocol protocol) {
        protocols.put(message, protocol);
    }

    public void run() {
        logger.info("Tcp Messaging Service is running");
        while (!isInterrupted() && !stop) {
            sendMessages();
            processIncomingMessages();
        }
        hasStopped = true;
        logger.info("Tcp Messaging Service stopped");
    }

    public void queueMessage(String message) {
        logger.info("queued new message " + message);
        outgoingMessages.add(message);
    }

    private void sendMessages() {
        while (!outgoingMessages.isEmpty())
            sendMessage();
    }

    void sendMessage() {
        try {
            String message = outgoingMessages.poll();
            encryptedTcpConnection.sendMessage(message);
        } catch (IOException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void processIncomingMessages() {
        while (processNextMessage() != null);
    }

    private String processNextMessage() {
        String message = null;
        try {
            message = encryptedTcpConnection.receiveMessage();
            boolean isProtocolMessage = Utils.hasProtocolHeader(message) && protocols.containsKey(Utils.extractProtocolHeader(message));
            if (isProtocolMessage) {
                logger.debug("is protocol message");
                String protocolHeader = Utils.extractProtocolHeader(message);
                ContextualProtocol contextualProtocol = protocols.get(protocolHeader);
                contextualProtocol.setContextValue(ContextValues.message, message);
                contextualProtocol.executeProtocol();
            } else {
                logger.info("Received message " + username + " " + partner + " " + message + " " + this);
                onChatMessageProtocol.setContextValue(ContextValues.message, message);
                onChatMessageProtocol.executeProtocol();
            }
        } catch (IOException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException e) {
            if (e.getMessage().equals("Connection reset")) {// TODO: DO A BETTER JOB OF HANDLING THIS
                logger.info("connection was reset, interrupting tcp service and shutting down");
                message = null;
                stop = true;
                System.out.println("Disconnected by peer");
            }
            if (!e.getMessage().equals("Read timed out") && !e.getMessage().equals("Connection reset"))
                e.printStackTrace();
            if (protocols.containsKey(e.getMessage()))
                protocols.get(e.getMessage()).executeProtocol();
        }
        return message;
    }

    public void shutdown() {
        interrupt();
        while (!hasStopped) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
