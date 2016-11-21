package edu.utdallas.cs5390.chat.framework.server.service;

import edu.utdallas.cs5390.chat.framework.common.ContextValues;
import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.connection.udp.EncryptedUDPMessageSenderSocket;
import edu.utdallas.cs5390.chat.framework.common.connection.udp.UDPMessageReceiverSocket;
import edu.utdallas.cs5390.chat.framework.common.connection.udp.UDPMessageSenderSocket;
import edu.utdallas.cs5390.chat.framework.common.util.Utils;

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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Adisor on 10/1/2016.
 */

// TODO: NEED TO ANALYZE EXCEPTIONS
public class ServerUDPService extends Thread {
    private UDPMessageReceiverSocket receiverSocket;
    private UDPMessageSenderSocket senderSocket;
    private EncryptedUDPMessageSenderSocket encryptedSenderSocket;
    private Map<String, ContextualProtocol> protocols;

    public ServerUDPService(int port) throws SocketException {
//        DatagramSocket datagramSocket = new DatagramSocket(port);
        receiverSocket = new UDPMessageReceiverSocket(port);
        senderSocket = new UDPMessageSenderSocket();
        encryptedSenderSocket = new EncryptedUDPMessageSenderSocket();
        this.protocols = new HashMap<>();
    }

    public void addProtocol(String protocolMessage, ContextualProtocol protocol){
        protocols.put(protocolMessage, protocol);
    }

    public void sendMessage(String message, InetAddress receiverAddress, int receiverPort) throws Exception {
        System.out.println(receiverAddress + " " + receiverPort + " " + message);
        senderSocket.sendMessage(message, receiverAddress, receiverPort);
    }

    public void sendEncryptedMessage(String message, InetAddress receiverAddress, int receiverPort, Key encryptionKey) throws IllegalBlockSizeException, NoSuchAlgorithmException, IOException, BadPaddingException, NoSuchPaddingException, InvalidKeyException {
        encryptedSenderSocket.setEncryptionKey(encryptionKey);
        encryptedSenderSocket.sendMessage(message, receiverAddress, receiverPort);
    }

    public void run() {
        System.out.println("Running");
        while (!isInterrupted()) {
            DatagramPacket receivedPacket;
            try {
                receivedPacket = receiverSocket.receivePacket();
            } catch (IOException e) {
                if(!e.getMessage().equals("Receive timed out"))
                    e.printStackTrace();
                continue;
            }
            String message = Utils.extractMessage(receivedPacket);
            System.out.println("Received message " + message);
            boolean isProtocolMessage = message.contains("(") && protocols.containsKey(message.substring(0, message.indexOf("(")));
            if (isProtocolMessage) {
                String protocolHeader = Utils.extractProtocolHeader(message);
                ContextualProtocol contextualProtocol = protocols.get(protocolHeader);
                contextualProtocol.setContextValue(ContextValues.packet, receivedPacket);
                contextualProtocol.executeProtocol();
            }
        }
    }

    public void close() {
        receiverSocket.close();
    }
}
