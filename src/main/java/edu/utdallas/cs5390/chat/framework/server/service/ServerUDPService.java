package edu.utdallas.cs5390.chat.framework.server.service;

import edu.utdallas.cs5390.chat.framework.common.ChatPacket;
import edu.utdallas.cs5390.chat.framework.common.ContextValues;
import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.connection.UdpConnection;
import edu.utdallas.cs5390.chat.framework.common.util.Utils;
import edu.utdallas.cs5390.chat.framework.server.ChatServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Adisor on 10/1/2016.
 */

// TODO: NEED TO ANALYZE EXCEPTIONS
public class ServerUDPService extends Thread {
    private final Logger logger = LoggerFactory.getLogger(ServerUDPService.class);
    private Map<String, ContextualProtocol> protocols;
    private DatagramSocket datagramSocket;
    private Map<String, UdpConnection> clientConnections;

    public ServerUDPService(ChatServer chatServer, int port) throws SocketException {
        this.protocols = new HashMap<>();
        clientConnections = new HashMap<>();
        datagramSocket = new DatagramSocket(port);
    }

    public void addProtocol(String protocolMessage, ContextualProtocol protocol) {
        protocols.put(protocolMessage, protocol);
        logger.debug("Added protocol for message " + protocolMessage);
    }

    public void run() {
        logger.debug("lala");
        while (!isInterrupted()) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(receivedPacket);
                String clientIp = receivedPacket.getAddress().getHostAddress();
                String address = clientIp + ":" + receivedPacket.getPort();
                logger.debug(String.format("Received new packet from %s:%d", clientIp, receivedPacket.getPort()));
                String message;
                if(clientConnections.containsKey(address)) {
                    message = clientConnections.get(address).receiveMessage();
                }else{
                    clientConnections.put(address, new UdpConnection(datagramSocket, clientIp, receivedPacket.getPort()));
                    message = new ChatPacket(receivedPacket.getData()).getMessage(null);
                    logger.debug("Creating udp connection with client");
                }
                String protocolHeader = Utils.extractProtocolHeader(message);
                logger.debug("Received message " + message);
                boolean isProtocolMessage = message.contains("(") && protocols.containsKey(protocolHeader);
                if (isProtocolMessage) {
                    ContextualProtocol contextualProtocol = protocols.get(protocolHeader);
                    contextualProtocol.setContextValue(ContextValues.message, message);
                    contextualProtocol.setContextValue(ContextValues.udpConnection, clientConnections.get(address));
                    contextualProtocol.executeProtocol();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (!e.getMessage().equals("Receive timed out"))
                    e.printStackTrace();
            }
        }
    }

    public void close() {
        Utils.closeResource(datagramSocket);
    }
}
