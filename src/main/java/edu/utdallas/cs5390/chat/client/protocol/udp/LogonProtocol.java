package edu.utdallas.cs5390.chat.client.protocol.udp;

import edu.utdallas.cs5390.chat.client.AbstractChatClient;
import edu.utdallas.cs5390.chat.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.common.connection.udp.UDPConnection;
import edu.utdallas.cs5390.chat.common.util.TransmissionException;
import edu.utdallas.cs5390.chat.common.util.Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by adisor on 10/30/2016.
 */
public class LogonProtocol extends ContextualProtocol {

    private final AbstractChatClient chatClient;
    private UDPConnection udpConnection;
    private Key secretKey;

    public LogonProtocol(AbstractChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public void executeProtocol(){
        try {
            udpConnection = chatClient.getUDPConnection();
            String username = chatClient.getUsername();
            String password = chatClient.getPassword();
            String response = udpConnection.sendMessageAndGetResponse(username);
            String rand = response.substring(response.indexOf(":") + 1);
            String cipherKey = Utils.createHash(MessageDigest.getInstance(Utils.SHA256), rand + password);
            secretKey = new SecretKeySpec(cipherKey.getBytes(), 0, 16, "AES");
            response = udpConnection.sendMessageAndGetResponse("key:" + cipherKey);
            response = Utils.cipherMessage(secretKey, Cipher.DECRYPT_MODE, response);
            boolean hasBeenAuthenticated = response.substring(response.indexOf(":") + 1).equals("true");
            if (hasBeenAuthenticated)
                attemptEstablishTCPConnection();
        } catch (TransmissionException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

    private void attemptEstablishTCPConnection() throws TransmissionException {
        String response = udpConnection.sendMessageAndGetResponse("register");
        if (response.equals("ok"))
                chatClient.startTCPMessagingService(chatClient.getServerAddress(), chatClient.getServerPort(), secretKey);
    }
}
