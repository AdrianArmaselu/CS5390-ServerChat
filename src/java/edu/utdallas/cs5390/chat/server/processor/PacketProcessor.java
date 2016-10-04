package edu.utdallas.cs5390.chat.server.processor;

import java.net.DatagramPacket;

/**
 * Created by Adisor on 10/1/2016.
 */
public interface PacketProcessor {
    void processPacket(DatagramPacket datagramPacket);
}
