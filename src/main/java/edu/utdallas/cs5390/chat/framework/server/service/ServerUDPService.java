package edu.utdallas.cs5390.chat.framework.server.service;

import edu.utdallas.cs5390.chat.framework.common.connection.udp.EncryptedUDPMessageSenderSocket;
import edu.utdallas.cs5390.chat.framework.common.connection.udp.UDPMessageReceiverSocket;
import edu.utdallas.cs5390.chat.framework.common.connection.udp.UDPMessageSenderSocket;
import edu.utdallas.cs5390.chat.framework.common.util.Utils;
import edu.utdallas.cs5390.chat.framework.server.AbstractChatServer;
import edu.utdallas.cs5390.chat.framework.server.protocol.ProtocolIncomingMessages;
import edu.utdallas.cs5390.chat.framework.server.protocol.ProtocolOutgoingMessages;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Adisor on 10/1/2016.
 */

// TODO: NEED TO ANALYZE EXCEPTIONS
public class ServerUDPService extends Thread {
    private static final int UDP_PORT = 8080;
    private static final int RAND_SIZE = 4;

    private AbstractChatServer chatServer;
    private UDPMessageReceiverSocket receiverSocket;
    private UDPMessageSenderSocket senderSocket;
    private EncryptedUDPMessageSenderSocket encryptedSenderSocket;

    public ServerUDPService(AbstractChatServer chatServer) throws SocketException {
        receiverSocket = new UDPMessageReceiverSocket(UDP_PORT);
        senderSocket = new UDPMessageSenderSocket();
        encryptedSenderSocket = new EncryptedUDPMessageSenderSocket();
        this.chatServer = chatServer;
    }

    private String extractMessage(DatagramPacket datagramPacket) {
        byte[] messageBytes = datagramPacket.getData();
        return new String(messageBytes);
    }

    public void run() {
        while (!isInterrupted()) {
            DatagramPacket incomingPacket;
            try {
                incomingPacket = receiverSocket.receivePacket();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            String message = extractMessage(incomingPacket);
            String ipAddress = incomingPacket.getAddress().getHostAddress();
            int port = incomingPacket.getPort();
            if (ProtocolIncomingMessages.isHelloMessage(message))
                logonClient(message, ipAddress, port);
            if (ProtocolIncomingMessages.isResponseMessage(message))
                authenticateClient(message, ipAddress, port);
            if (ProtocolIncomingMessages.isRegisteredMessage(message))
                registerClient(ipAddress);
        }
    }

    private void logonClient(String message, String ipAddress, int port) {
        String username = ProtocolIncomingMessages.extractUsername(message);
        if (chatServer.isASubscriber(username)) {
            chatServer.setId(username, ipAddress);
            String rand = Utils.randomString(RAND_SIZE);
            chatServer.saveRand(username, rand);
            try {
                senderSocket.sendMessage(ProtocolOutgoingMessages.CHALLENGE(rand), InetAddress.getByAddress(ipAddress.getBytes()), port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void authenticateClient(String message, String ipAddress, int port) {
        String res = ProtocolIncomingMessages.extractRes(message);
        String username = chatServer.getUsername(ipAddress);
        Key encryptionKey = chatServer.generateEncryptionKey(username);
        encryptedSenderSocket.setEncryptionKey(encryptionKey);
        try {
            InetAddress clientAddress = InetAddress.getByAddress(ipAddress.getBytes());
            if (chatServer.hasMatchingRes(username, res)) {
                encryptedSenderSocket.sendMessage(ProtocolOutgoingMessages.AUTH_SUCCESS, clientAddress, port);
            } else {
                encryptedSenderSocket.sendMessage(ProtocolOutgoingMessages.AUTH_FAIL, clientAddress, port);
            }
        } catch (IOException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

    private void registerClient(String ipAddress) {
        chatServer.acceptTCPConnectionFromUser(chatServer.getUsername(ipAddress)); // add to blockingqueue
    }

    public void close() {
        receiverSocket.close();
    }
}
