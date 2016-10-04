package edu.utdallas.cs5390.chat.server.service.udp;

import edu.utdallas.cs5390.chat.server.processor.PacketProcessor;
import edu.utdallas.cs5390.chat.util.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adisor on 10/1/2016.
 */
public class UDPConnectionService extends Thread {
    private static final int SOCKET_WAIT_TIMEOUT = Utils.seconds(1);
    private static final int PACKET_BUFFER_SIZE = Utils.kilobytes(1);
    private static final int UDP_PORT = 8080;

    private List<PacketProcessor> packetListeners;
    private DatagramSocket datagramSocket;

    public UDPConnectionService() throws SocketException {
        packetListeners = new ArrayList<PacketProcessor>();
        datagramSocket = new DatagramSocket(UDP_PORT);
        datagramSocket.setSoTimeout(SOCKET_WAIT_TIMEOUT);
    }

    public void addPacketListener(PacketProcessor packetProcessor){
        packetListeners.add(packetProcessor);
    }

    public void run() {
        byte[] buffer = new byte[PACKET_BUFFER_SIZE];
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
        try {
            datagramSocket.receive(datagramPacket);
            for (PacketProcessor listener : packetListeners)
                listener.processPacket(datagramPacket);
        } catch (IOException ignored) {
        }
    }

    public void close() {
        Utils.closeResource(datagramSocket);
    }
}
