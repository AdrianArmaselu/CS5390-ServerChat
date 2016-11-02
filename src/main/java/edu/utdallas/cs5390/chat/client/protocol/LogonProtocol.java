package edu.utdallas.cs5390.chat.client.protocol;

import edu.utdallas.cs5390.chat.client.AbstractChatClient;
import edu.utdallas.cs5390.chat.client.connection.EncryptedTCPClientConnection;
import edu.utdallas.cs5390.chat.client.connection.UDPClientConnection;
import edu.utdallas.cs5390.chat.util.TransmissionException;
import edu.utdallas.cs5390.chat.util.Utils;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by adisor on 10/30/2016.
 */
public class LogonProtocol implements ClientProtocol {

    private AbstractChatClient chatClient;
    private UDPClientConnection udpConnection;
    private Key secretKey;

    @Override
    public void executeProtocol(AbstractChatClient chatClient) throws Exception {
        this.chatClient = chatClient;
        udpConnection = chatClient.getUdpConnection();
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
    }

    private void attemptEstablishTCPConnection() throws TransmissionException {
        String response = udpConnection.sendMessageAndGetResponse("register");
        if (response.equals("ok")) {
            try {
                chatClient.setTcpConnection(new EncryptedTCPClientConnection(chatClient.getHost(), chatClient.getPort(), secretKey));
            } catch (IOException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }
}
