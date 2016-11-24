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
            retries++;
            try {
                sendMessage(message);
                response = receiveMessage();
            } catch (Exception e) {
                if(e.getMessage().equals("Receive timed out"))
                    logger.warn(e.getMessage());
                else
                    e.printStackTrace();
                response = null;
            }
        } while (response == null && retries < MAX_RETRIES);
        if (retries == MAX_RETRIES) throw new TimeoutException("Server either refused to respond or is unreachable");
        return response;
    }

    public abstract void sendMessage(String message) throws Exception;

    public abstract String receiveMessage() throws Exception;

    public abstract void close();
}
