package edu.utdallas.cs5390.chat.server.service.udp;

import edu.utdallas.cs5390.chat.server.AbstractChatServer;
import edu.utdallas.cs5390.chat.server.protocol.ProtocolIncomingMessages;
import edu.utdallas.cs5390.chat.server.protocol.ProtocolOutgoingMessages;
import edu.utdallas.cs5390.chat.util.Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Adisor on 10/1/2016.
 */

// TODO: NEED TO ANALYZE EXCEPTIONS
public class UDPConnectionService extends Thread {
    private static final int SOCKET_WAIT_TIMEOUT = Utils.seconds(1);
    private static final int PACKET_BUFFER_SIZE = Utils.kilobytes(1);
    private static final int UDP_PORT = 8080;
    private static final int RAND_SIZE = 4;

    private DatagramSocket datagramSocket;
    private AbstractChatServer chatServer;
    private Key encryptionKey;

    public UDPConnectionService(AbstractChatServer chatServer) throws SocketException {
        datagramSocket = new DatagramSocket(UDP_PORT);
        datagramSocket.setSoTimeout(SOCKET_WAIT_TIMEOUT);
        this.chatServer = chatServer;
    }

    private DatagramPacket receivePacket() throws IOException {
        byte[] buffer = new byte[PACKET_BUFFER_SIZE];
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
        datagramSocket.receive(datagramPacket);
        return datagramPacket;
    }

    private String extractMessage(DatagramPacket datagramPacket) {
        byte[] messageBytes = datagramPacket.getData();
        return new String(messageBytes);
    }

    private String receiveMessage() throws IOException {
        return extractMessage(receivePacket());
    }

    private void sendMessage(String message) throws IOException {
        datagramSocket.send(new DatagramPacket(message.getBytes(), message.length()));
    }

    private void sendEncryptedMessage(String message) {
        try {
            sendMessage(Utils.cipherMessage(encryptionKey, Cipher.ENCRYPT_MODE, message));
        } catch (BadPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (!isInterrupted()) {
            DatagramPacket incomingPacket;
            try {
                incomingPacket = receivePacket();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            String message = extractMessage(incomingPacket);
            String ipAddress = incomingPacket.getAddress().getHostAddress();
            if (ProtocolIncomingMessages.isHelloMessage(message))
                logonClient(message, ipAddress);
            if (ProtocolIncomingMessages.isResponseMessage(message))
                authenticateClient(message, ipAddress);
            if (ProtocolIncomingMessages.isRegisteredMessage(message))
                registerClient(ipAddress);
        }
    }

    private void logonClient(String message, String ipAddress) {
        String username = ProtocolIncomingMessages.extractUsername(message);
        if (chatServer.isASubscriber(username)) {
            chatServer.setId(username, ipAddress);
            String rand = Utils.randomString(RAND_SIZE);
            chatServer.saveRand(username, rand);
            try {
                sendMessage(ProtocolOutgoingMessages.CHALLENGE(rand));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void authenticateClient(String message, String ipAddress) {
        String res = ProtocolIncomingMessages.extractRes(message);
        String username = chatServer.getUsername(ipAddress);
        encryptionKey = chatServer.generateEncryptionKey();
        sendEncryptedMessage(chatServer.hasMatchingCipherKey(username, res) ? ProtocolOutgoingMessages.AUTH_SUCCESS : ProtocolOutgoingMessages.AUTH_FAIL);
    }

    private void registerClient(String ipAddress) {
        chatServer.acceptTCPConnectionFromUser(chatServer.getUsername(ipAddress)); // add to blockingqueue
    }

    public void close() {
        Utils.closeResource(datagramSocket);
    }
}
