package edu.utdallas.cs5390.chat.impl.client;

import edu.utdallas.cs5390.chat.framework.client.AbstractChatClient;
import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.connection.UdpConnection;
import edu.utdallas.cs5390.chat.framework.common.util.Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * Created by adisor on 10/30/2016.
 */
class LogonProtocol extends ContextualProtocol {

    private final AbstractChatClient chatClient;
    private UdpConnection udpConnection;
    private Key encryptionKey;

    LogonProtocol(AbstractChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public void executeProtocol() {

        System.out.println("Logging in...");

        try {

            udpConnection = chatClient.getUDPConnection();
            System.out.println("Retrieving challenge code...");

            String rand = retrieveRand();
            System.out.println("Creating keys...");

            String cipherKey = createSecretKey(rand);

            if (authenticateWithServer(cipherKey)) {
                System.out.println("Authenticating with the server...");
                attemptEstablishTCPConnection();
            }
            else System.out.println("Could not Authenticate with the server");
        } catch (TimeoutException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        System.out.println("Logged in to the server");
    }

    private String retrieveRand() throws TimeoutException {
        return ProtocolServerResponses.extractValue(udpConnection.sendMessageAndGetResponse(ProtocolServerRequests.HELLO(chatClient.getUsername())));
    }

    private String createSecretKey(String rand) throws NoSuchAlgorithmException {
        String cipherKey = Utils.createCipherKey(rand, chatClient.getPassword());
        encryptionKey = Utils.createEncryptionKey(cipherKey);
        return cipherKey;
    }

    private boolean authenticateWithServer(String cipherKey) throws IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, TimeoutException {
        udpConnection.addReceiverEncryption(encryptionKey);
        String response = udpConnection.sendMessageAndGetResponse(ProtocolServerRequests.RESPONSE(cipherKey));
        return ProtocolServerResponses.isAuthSuccessful(response);
    }

    private void attemptEstablishTCPConnection() throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, TimeoutException {
        udpConnection.addSenderEncryption(encryptionKey);
        String message = udpConnection.sendMessageAndGetResponse(ProtocolServerRequests.REGISTER(""));
        if (ProtocolServerResponses.isRegistered(message))
            chatClient.startTCPMessagingService(encryptionKey);
    }
}
