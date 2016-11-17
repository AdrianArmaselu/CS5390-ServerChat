package edu.utdallas.cs5390.chat.common.connection.udp;

import edu.utdallas.cs5390.chat.common.connection.AbstractConnection;

import java.net.*;

/**
 * Created by adisor on 11/1/2016.
 */
public class UDPConnection extends AbstractConnection{
    private UDPMessageSenderSocket udpMessageSenderSocket;
    private UDPMessageReceiverSocket udpMessageReceiverSocket;
    private InetAddress serverAddress;
    private int serverPort;

    public UDPConnection(String serverAddress, int serverPort) throws UnknownHostException, SocketException {
        this.serverAddress = InetAddress.getByAddress(serverAddress.getBytes());
        this.serverPort = serverPort;
        udpMessageSenderSocket = new UDPMessageSenderSocket();
        udpMessageReceiverSocket = new UDPMessageReceiverSocket(serverPort);
    }

    @Override
    public void sendMessage(String message) throws Exception {
        udpMessageSenderSocket.sendMessage(message, serverAddress, serverPort);
    }

    @Override
    public String receiveMessage() throws Exception {
        return udpMessageReceiverSocket.receiveMessage();
    }

    @Override
    public void close() {
        udpMessageSenderSocket.close();
        udpMessageReceiverSocket.close();
    }
}
