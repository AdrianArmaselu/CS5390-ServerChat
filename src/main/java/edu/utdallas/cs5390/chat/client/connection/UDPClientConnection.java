package edu.utdallas.cs5390.chat.client.connection;

import edu.utdallas.cs5390.chat.util.Utils;

import java.io.IOException;
import java.net.*;

/**
 * Created by Adisor on 10/1/2016.
 */

public class UDPClientConnection extends AbstractClientConnection {
    private static final int PACKET_BUFFER_SIZE = Utils.kilobytes(1);
    private final DatagramSocket datagramSocket;

    public UDPClientConnection(String hostAddress, int hostPort) throws SocketException {
        datagramSocket = new DatagramSocket(new InetSocketAddress(hostAddress, hostPort));
        datagramSocket.setSoTimeout(RECEIVE_TIMEOUT);
    }

    public void sendMessage(String message) throws IOException {
        datagramSocket.send(new DatagramPacket(message.getBytes(), message.length()));
    }

    public String receiveMessage() throws IOException {
        byte[] buffer = new byte[PACKET_BUFFER_SIZE];
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
        datagramSocket.receive(datagramPacket);
        return new String(datagramPacket.getData());
    }

    public void close() {
        Utils.closeResource(datagramSocket);
    }
}
