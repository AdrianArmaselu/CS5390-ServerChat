package edu.utdallas.cs5390.chat.client.connection;

import edu.utdallas.cs5390.chat.util.TransmissionException;
import edu.utdallas.cs5390.chat.util.Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;

/**
 * Created by Adisor on 10/1/2016.
 */
public abstract class AbstractClientConnection {
    private static final int MAX_RETRIES = 3;
    static final int RECEIVE_TIMEOUT = Utils.seconds(1);

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
        if (retries == MAX_RETRIES) throw new TransmissionException("No response received or host is unreachable");
        return response;
    }

    public abstract void sendMessage(String message) throws Exception;

    public abstract String receiveMessage() throws Exception;

    public abstract void close();
}
