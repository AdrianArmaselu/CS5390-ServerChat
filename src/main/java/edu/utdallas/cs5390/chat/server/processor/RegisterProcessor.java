package edu.utdallas.cs5390.chat.server.processor;

import edu.utdallas.cs5390.chat.server.ChatServer;

import java.net.DatagramPacket;

/**
 * Created by Adisor on 10/1/2016.
 */
public class RegisterProcessor implements PacketProcessor {
    ChatServer chatServer;

    public RegisterProcessor(ChatServer chatServer) {
        this.chatServer = chatServer;
    }

    @Override
    public void processPacket(DatagramPacket datagramPacket) {

    }
}
//