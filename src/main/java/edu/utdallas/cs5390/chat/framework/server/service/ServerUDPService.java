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
            ClientProfile clientProfile = chatServer.getProfileByUsername(username);
            chatServer.addIpProfile(ipAddress, clientProfile);
            clientProfile.rand = Utils.randomString(RAND_SIZE);
            try {
                senderSocket.sendMessage(ProtocolOutgoingMessages.CHALLENGE(clientProfile.rand), InetAddress.getByAddress(ipAddress.getBytes()), port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void authenticateClient(String message, String ipAddress, int port) {
        String clientRes = ProtocolIncomingMessages.extractRes(message);
        ClientProfile clientProfile = chatServer.getProfileByIp(ipAddress);
        try {
            String serverRes = Utils.createCipherKey(clientProfile.rand, clientProfile.password);
            Key encryptionKey = Utils.createEncryptionKey(serverRes);
            encryptedSenderSocket.setEncryptionKey(encryptionKey);
            InetAddress clientAddress = InetAddress.getByAddress(ipAddress.getBytes());
            boolean authenticationCodesMatch = clientRes.equals(serverRes);
            if (authenticationCodesMatch) {
                encryptedSenderSocket.sendMessage(ProtocolOutgoingMessages.AUTH_SUCCESS, clientAddress, port);
            } else {
                encryptedSenderSocket.sendMessage(ProtocolOutgoingMessages.AUTH_FAIL, clientAddress, port);
            }
        } catch (IOException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

    private void registerClient(String ipAddress) {
        ClientProfile clientProfile = chatServer.getProfileByIp(ipAddress);
        clientProfile.isRegistered = true;
    }

    public void close() {
        receiverSocket.close();
    }
}
