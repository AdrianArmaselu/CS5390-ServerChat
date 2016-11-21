package edu.utdallas.cs5390.chat.framework.common.connection;

import edu.utdallas.cs5390.chat.framework.common.util.TransmissionException;

/**
 * Created by Adisor on 10/1/2016.
 */
public abstract class AbstractConnection {
    private static final int MAX_RETRIES = 3;
    static final int RECEIVE_TIMEOUT = 1000;

    public AbstractConnection() {
    }

    public String sendMessageAndGetResponse(String message) throws TransmissionException {
        String response = null;
        int retries = 0;
        do {
            retries++;
            try {
                sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            try {
                response = receiveMessage();
            } catch (Exception e) {
                e.printStackTrace();
                response = null;
            }

        } while (response == null && retries < MAX_RETRIES);
        if (retries == MAX_RETRIES) throw new TransmissionException("No message received or host is unreachable");
        return response;
    }

    public abstract void sendMessage(String message) throws Exception;

    public abstract String receiveMessage() throws Exception;

    public abstract void close();
}
