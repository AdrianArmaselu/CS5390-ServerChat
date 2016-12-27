package edu.utdallas.cs5390.chat.framework.common.connection;

import edu.utdallas.cs5390.chat.framework.common.ChatPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.security.Key;
import java.util.Arrays;

/**
 * Created by adisor on 11/1/2016.
 */
// TODO: SERVERS DO NOT HAVE RECEIVE TIMEOUT
public class UdpConnection extends AbstractConnection {
    private static final Logger logger = LoggerFactory.getLogger(UdpConnection.class);
    private static final int PACKET_BUFFER_SIZE = 1024;
    private DatagramSocket datagramSocket;
    private String destinationIpAddress;
    private int destinationPort;
    private Key senderEncryptionKey;
    private Key receiverEncryptionKey;

    // used to create a new connection but reuse the listener socket
    public UdpConnection(DatagramSocket datagramSocket, String destinationIpAddress, int destinationPort) throws UnknownHostException, SocketException {
        this(datagramSocket);
        this.destinationIpAddress = destinationIpAddress;
        this.destinationPort = destinationPort;
        logger.debug("Udp connection created with ip address " + destinationIpAddress + " and destination port " + destinationPort);
    }

    // used solely as a listener
    public UdpConnection(DatagramSocket datagramSocket) throws UnknownHostException, SocketException {
        logger.debug("Creating udp connection with datagram socket " + datagramSocket);
        this.datagramSocket = datagramSocket;
    }

    // used when you do not care about on which port to listen but want to communicate with a specific destination
    public UdpConnection(String destinationIpAddress, int destinationPort) throws UnknownHostException, SocketException {
        this();
        this.destinationIpAddress = destinationIpAddress;
        this.destinationPort = destinationPort;
        logger.debug("Udp connection created with ip address " + destinationIpAddress + " and destination port " + destinationPort);
    }

    public UdpConnection() throws UnknownHostException, SocketException {
        logger.debug("Creating udp connection with datagram socket " + datagramSocket);
        datagramSocket = new DatagramSocket();
        datagramSocket.setSoTimeout(RECEIVE_TIMEOUT);
    }

    public void addEncryption(Key encryptionKey) {
        addSenderEncryption(encryptionKey);
        addReceiverEncryption(encryptionKey);
        logger.debug("added encryption to both sender and receiver");
    }

    public void addSenderEncryption(Key encryptionKey) {
        senderEncryptionKey = encryptionKey;
    }

    public void addReceiverEncryption(Key encryptionKey) {
        receiverEncryptionKey = encryptionKey;
    }

    public void setDestination(String destinationAddress, int destinationPort) {
        setDestinationIpAddress(destinationAddress);
        setDestinationPort(destinationPort);
    }

    public void setDestinationIpAddress(String destinationIpAddress) {
        this.destinationIpAddress = destinationIpAddress;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    @Override
    public void sendMessage(String message) throws Exception {
        logger.debug("Sending message " + message);
        logger.debug("Creating chat packet with encryption key " + senderEncryptionKey);
        ChatPacket chatPacket = new ChatPacket(message, senderEncryptionKey);
        logger.debug("Chat packet created");
        DatagramPacket datagramPacket = new DatagramPacket(chatPacket.getData(), chatPacket.getData().length, InetAddress.getByName(destinationIpAddress), destinationPort); // TODO : THIS COULD BE MORE EFFICIENT
        logger.debug("Created datagram packet with the chat packet data");
        logger.debug(
                String.format("Sending udp packet to %s:%d with string: %s ; bytes: %s. Has encryption flag %b",
                        destinationIpAddress,
                        destinationPort,
                        message,
                        Arrays.toString(datagramPacket.getData()),
                        chatPacket.isEncrypted()
                )
        );
        datagramSocket.send(datagramPacket);
        logger.debug("datagram packet sent");
    }

    public DatagramPacket receivePacket() throws IOException { // TODO: CAN CHANGE TO FIRST GET THE SIZE OF THE MESSAGE, AND THEN ACTUALLY RECEIVE THE MESSAGE
        byte[] buffer = new byte[PACKET_BUFFER_SIZE];
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
        datagramSocket.receive(datagramPacket);
        logger.debug(
                String.format("Received udp packet from %s:%d with bytes: %s",
                        datagramPacket.getAddress().getHostAddress(),
                        datagramPacket.getPort(),
                        Arrays.toString(datagramPacket.getData())
                )
        );
        return datagramPacket;
    }

    @Override
    public String receiveMessage() throws Exception {
        ChatPacket chatPacket = new ChatPacket(receivePacket().getData());
        logger.debug(String.format("Received chat packet with message %s. Has encryption flag %b", chatPacket.getMessage(receiverEncryptionKey), chatPacket.isEncrypted()));
        return chatPacket.getMessage(receiverEncryptionKey);
    }

    @Override
    public void close() {
        datagramSocket.close();
    }

    @Override
    public String getDestinationAddress() {
        return getIpAddress() + ":" + getDestinationPort();
    }

    public String getIpAddress() {
        return destinationIpAddress;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public int getPort() {
        return datagramSocket.getLocalPort();
    }
}
