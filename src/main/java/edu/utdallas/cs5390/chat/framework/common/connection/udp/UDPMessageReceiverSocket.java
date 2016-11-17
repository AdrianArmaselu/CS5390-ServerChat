package edu.utdallas.cs5390.chat.framework.common.connection.udp;

import edu.utdallas.cs5390.chat.framework.common.util.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by adisor on 11/1/2016.
 */
public class UDPMessageReceiverSocket extends UDPMessageSocket {
    private static final int PACKET_BUFFER_SIZE = Utils.kilobytes(1);
    private static final int RECEIVE_TIMEOUT = 100;

    public UDPMessageReceiverSocket(int udpPort) throws SocketException {
        this(new DatagramSocket(udpPort));
        datagramSocket.setSoTimeout(RECEIVE_TIMEOUT);
    }

    private UDPMessageReceiverSocket(DatagramSocket datagramSocket){
        super(datagramSocket);
    }

    public DatagramPacket receivePacket() throws IOException {
        byte[] buffer = new byte[PACKET_BUFFER_SIZE];
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
        datagramSocket.receive(datagramPacket);
        return datagramPacket;
    }

    public String receiveMessage() throws IOException {
        return new String(receivePacket().getData());
    }
}
