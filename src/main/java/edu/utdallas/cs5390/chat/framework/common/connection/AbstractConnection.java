package edu.utdallas.cs5390.chat.framework.common.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

/**
 * Created by Adisor on 10/1/2016.
 */
public abstract class AbstractConnection {
    private final Logger logger = LoggerFactory.getLogger(AbstractConnection.class);

    private static final int MAX_RETRIES = 3;
    protected static final int RECEIVE_TIMEOUT = 1000;

    public AbstractConnection() {
    }

    public String sendMessageAndGetResponse(String message) throws TimeoutException {
        String response;
        int retries = 0;
        do {
            logger.debug("Attempting to transmit message. Attempts remaining: " + (MAX_RETRIES - retries));
            retries++;
            try {
                sendMessage(message);
                logger.debug("Transmitted message successfully");
                logger.debug("Waiting to receive response");
                response = receiveMessage();
                logger.debug("Received response successfully");
            } catch (Exception e) {
                logger.debug("Either message was not transmitted or no response received. Reason is " + e.getMessage());
                if (e.getMessage().equals("Receive timed out"))
                    logger.warn(e.getMessage());
                else
                    logger.error(e.getMessage(), e);
                response = null;
            }
        } while (response == null && retries < MAX_RETRIES);
        if (retries == MAX_RETRIES) throw new TimeoutException("Server either refused to respond or is unreachable");
        return response;
    }

    public abstract void sendMessage(String message) throws Exception;

    public abstract String receiveMessage() throws Exception;

    public abstract void close();

    public abstract String getDestinationAddress();

    public abstract String getIpAddress();
}
