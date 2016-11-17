package edu.utdallas.cs5390.chat.framework.common.connection.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by Adisor on 10/1/2016.
 */

public class UDPMessageSenderSocket extends UDPMessageSocket {
    public UDPMessageSenderSocket() throws SocketException {
        super(new DatagramSocket());
    }
    public void sendMessage(String message, InetAddress receiverAddress, int receiverPort) throws Exception{
        datagramSocket.send(new DatagramPacket(message.getBytes(), message.length(), receiverAddress, receiverPort));
    }
}
