package edu.utdallas.cs5390.chat.framework.common.service;

import edu.utdallas.cs5390.chat.framework.common.connection.AbstractConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by adisor on 10/30/2016.
 */

public abstract class MessagingService extends Thread {
    private final Logger logger = LoggerFactory.getLogger(MessagingService.class);
    private AbstractConnection connection;
    private BlockingQueue<String> outgoingMessages;
    private boolean stop;
    private boolean messagesSent;

    public MessagingService(AbstractConnection connection) {
        this();
        this.connection = connection;
    }

    public MessagingService() {
        outgoingMessages = new LinkedBlockingQueue<>();
    }

    public void setConnection(AbstractConnection connection) {
        this.connection = connection;
    }

    public void run() {
        while (!isInterrupted() && !stop) {
            sendMessages();
            processIncomingMessages();
        }
        logger.debug("Stopped");
    }

    public void queueMessage(String message) {
        outgoingMessages.add(message);
        logger.info("Queued message " + message);
        messagesSent = false;
    }

    private void sendMessages() {
        while (!outgoingMessages.isEmpty())
            sendMessage();
    }

    protected String retrieveMessage() throws Exception {
        return connection.receiveMessage();
    }

    public boolean areMessagesSent() {
        return outgoingMessages.isEmpty() && messagesSent;
    }

    void sendMessage() {
        try {
            String message = outgoingMessages.poll();
            connection.sendMessage(message);
            logger.info("Sent message " + message);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        messagesSent = true;
    }

    private void processIncomingMessages() {
        try {
            while (!isInterrupted() && !stop) {
                processNextMessage();
                logger.info("Processed message");
            }
        } catch (Exception e) {
            if (e.getMessage() != null) {
                if (!e.getMessage().equals("Read timed out") && !e.getMessage().equals("Connection reset") && !e.getMessage().equals("Socket Closed"))
                    logger.error(e.getMessage(), e);
                if (e.getMessage().equals("Connection reset")) {
                    System.out.println("Connection reset by peer. Closing connection.");
                    shutdown();
                }
            }
        }
    }

    protected abstract void processNextMessage() throws Exception;

    public void shutdown() {
        logger.info("Shutting down messaging service");
        interrupt();
        stop = true;
        connection.close();
        logger.info("Service shutdown successfully");
    }
}
