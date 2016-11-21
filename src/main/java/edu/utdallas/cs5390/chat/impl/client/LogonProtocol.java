package edu.utdallas.cs5390.chat.impl.client;

import edu.utdallas.cs5390.chat.framework.client.AbstractChatClient;
import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.connection.udp.UDPConnection;
import edu.utdallas.cs5390.chat.framework.common.util.TransmissionException;
import edu.utdallas.cs5390.chat.framework.common.util.Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * Created by adisor on 10/30/2016.
 */
class LogonProtocol extends ContextualProtocol {

    private final AbstractChatClient chatClient;
    private UDPConnection udpConnection;
    private Key encryptionKey;

    LogonProtocol(AbstractChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public void executeProtocol() {
        try {
            udpConnection = chatClient.getUDPConnection();
            String rand = retrieveRand();
            String cipherKey = createSecretKey(rand);
            if (authenticateWithServer(cipherKey))
                attemptEstablishTCPConnection();
            else
                System.out.println("Could not Authenticate with the server");
        } catch (TransmissionException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

    private String retrieveRand() throws TransmissionException {
        String response = udpConnection.sendMessageAndGetResponse(ProtocolServerRequests.HELLO(chatClient.getUsername()));
        return ProtocolServerResponses.extractValue(response);
    }

    private String createSecretKey(String rand) throws NoSuchAlgorithmException {
        String cipherKey = Utils.createCipherKey(rand, chatClient.getPassword());
        encryptionKey = Utils.createEncryptionKey(cipherKey);
        return cipherKey;
    }

    private boolean authenticateWithServer(String cipherKey) throws TransmissionException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException {
        String response = udpConnection.sendMessageAndGetResponse(ProtocolServerRequests.RESPONSE(cipherKey));
        System.out.println(response + " " + response.length());
        System.out.println(response.length());
        response = Utils.cipherMessage(encryptionKey, Cipher.DECRYPT_MODE, response);
        return ProtocolServerResponses.isAuthSuccessful(response);
    }

    private void attemptEstablishTCPConnection() throws TransmissionException {
        String response = udpConnection.sendMessageAndGetResponse(ProtocolServerRequests.REGISTER(""));
        if (ProtocolServerResponses.isRegistered(response))
            chatClient.startTCPMessagingService(chatClient.getServerAddress(), chatClient.getServerTcpPort(), encryptionKey);
    }
}
