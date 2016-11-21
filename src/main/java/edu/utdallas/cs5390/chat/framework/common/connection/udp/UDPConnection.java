package edu.utdallas.cs5390.chat.framework.common.connection.udp;

import edu.utdallas.cs5390.chat.framework.common.connection.AbstractConnection;

import java.net.*;

/**
 * Created by adisor on 11/1/2016.
 */
public class UDPConnection extends AbstractConnection{
    private UDPMessageSenderSocket udpMessageSenderSocket;
    private UDPMessageReceiverSocket udpMessageReceiverSocket;
    private InetAddress serverAddress;
    private int serverPort;

    public UDPConnection(String serverAddress, int clientPort, int serverPort) throws UnknownHostException, SocketException {
        this.serverAddress = InetAddress.getByName(serverAddress);
        this.serverPort = serverPort;
        udpMessageReceiverSocket = new UDPMessageReceiverSocket(clientPort);
        udpMessageSenderSocket = new UDPMessageSenderSocket();
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
