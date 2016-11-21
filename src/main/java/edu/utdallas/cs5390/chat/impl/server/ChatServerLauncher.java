package edu.utdallas.cs5390.chat.impl.server;

import com.beust.jcommander.JCommander;
import edu.utdallas.cs5390.chat.framework.client.ChatClientArguments;
import edu.utdallas.cs5390.chat.framework.common.ContextValues;
import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.service.TCPMessagingService;
import edu.utdallas.cs5390.chat.framework.common.util.CLIReader;
import edu.utdallas.cs5390.chat.framework.common.util.Utils;
import edu.utdallas.cs5390.chat.framework.server.AbstractChatServer;
import edu.utdallas.cs5390.chat.framework.server.ChatServer;
import edu.utdallas.cs5390.chat.framework.server.ChatServerArguments;
import edu.utdallas.cs5390.chat.framework.server.service.ClientProfile;
import edu.utdallas.cs5390.chat.framework.server.service.ServerUDPService;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * Created by adisor on 11/20/2016.
 */

// TODO: IF A MESSAGE NEEDS TO BE RETRANSMITTED, DO NOT RE-EXECUTE THE CODE
class ChatServerLauncher {
    private AbstractChatServer chatServer;

    private void run(String[] args) throws IOException {
        ChatServerArguments chatServerArguments = new ChatServerArguments();
        JCommander jCommander = new JCommander(chatServerArguments);
        jCommander.parse(args);
        chatServer = new ChatServer(chatServerArguments);
        addCLIProtocols();
        addUdpProtocols();
        addTCPProtocols();
        try {
            //setup
            AbstractChatServer chatServer = new ChatServer(chatServerArguments);
            CLIReader cliReader = new CLIReader();
            String command = "";

            // startup
            chatServer.startup();
            while (!command.equals("exit")) {
                command = cliReader.readInput();
            }
            chatServer.shutdown();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void printUsage() {
        new JCommander(new ChatClientArguments()).usage();
    }

    private void addCLIProtocols() {
    }

    private void addUdpProtocols() {
        chatServer.addUdpProtocol(ProtocolIncomingUdpMessages.HELLO, new ContextualProtocol() {
            @Override
            public void executeProtocol() {
                DatagramPacket datagramPacket = (DatagramPacket) getContextValue(ContextValues.packet);
                String message = Utils.extractMessage(datagramPacket);
                String username = Utils.extractValue(message);
                String clientIp = datagramPacket.getAddress().getHostAddress();
                int port = datagramPacket.getPort();
                if (chatServer.isASubscriber(username)) {
                    ClientProfile clientProfile = chatServer.getProfileByUsername(username);
                    chatServer.addIpProfile(clientIp, clientProfile);
                    clientProfile.rand = Utils.randomString(4);
                    try {
                        ServerUDPService serverUDPService = chatServer.getUdpService();
                        serverUDPService.sendMessage(ProtocolOutgoingMessages.CHALLENGE(clientProfile.rand), InetAddress.getByAddress(clientIp.getBytes()), port);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        chatServer.addUdpProtocol(ProtocolIncomingUdpMessages.RESPONSE, new ContextualProtocol() {
            @Override
            public void executeProtocol() {
                DatagramPacket datagramPacket = (DatagramPacket) getContextValue(ContextValues.packet);
                String message = Utils.extractMessage(datagramPacket);
                String clientRes = Utils.extractValue(message);
                String clientIp = datagramPacket.getAddress().getHostAddress();
                int clientPort = datagramPacket.getPort();
                ClientProfile clientProfile = chatServer.getProfileByIp(clientIp);
                try {
                    String serverRes = Utils.createCipherKey(clientProfile.rand, clientProfile.password);
                    Key encryptionKey = Utils.createEncryptionKey(serverRes);
                    ServerUDPService serverUDPService = chatServer.getUdpService();
                    boolean authenticationCodesMatch = clientRes.equals(serverRes);
                    if (authenticationCodesMatch) {
                        serverUDPService.sendEncryptedMessage(ProtocolOutgoingMessages.AUTH_SUCCESS, InetAddress.getByAddress(clientIp.getBytes()), clientPort, encryptionKey);
                    } else {
                        serverUDPService.sendEncryptedMessage(ProtocolOutgoingMessages.AUTH_FAIL, InetAddress.getByAddress(clientIp.getBytes()), clientPort, encryptionKey);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        chatServer.addUdpProtocol(ProtocolIncomingUdpMessages.REGISTER, new ContextualProtocol() {
            @Override
            public void executeProtocol() {
                DatagramPacket datagramPacket = (DatagramPacket) getContextValue(ContextValues.packet);
                String clientIp = datagramPacket.getAddress().getHostAddress();
                ClientProfile clientProfile = chatServer.getProfileByIp(clientIp);
                clientProfile.isRegistered = true;
                ServerUDPService serverUDPService = chatServer.getUdpService();
                try {
                    serverUDPService.sendEncryptedMessage(
                            ProtocolOutgoingMessages.AUTH_SUCCESS,
                            InetAddress.getByAddress(clientIp.getBytes()),
                            datagramPacket.getPort(),
                            clientProfile.encryptionKey
                    );
                } catch (IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | IOException | InvalidKeyException | NoSuchPaddingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void addTCPProtocols() {
        chatServer.addTcpProtocol(ProtocolIncomingTcpMessages.CONNECT, new ContextualProtocol() {
            @Override
            public void executeProtocol() {
                TCPMessagingService tcpMessagingService = (TCPMessagingService) getContextValue(ContextValues.tcpMessagingService);
                ClientProfile clientProfile = (ClientProfile) getContextValue(ContextValues.clientProfile);
                String message = (String) getContextValue(ContextValues.message);
                String targetUsername = Utils.extractValue(message);
                ClientProfile targetProfile = chatServer.getProfileByUsername(targetUsername);
                if(targetProfile.isRegistered && !targetProfile.inSession){
                    String sessionId = Utils.UUID();
                    TCPMessagingService targetMessagingService = chatServer.getTcpMessagingService(targetUsername);
                    tcpMessagingService.queueMessage(ProtocolOutgoingMessages.START(sessionId, targetProfile.username));
                    targetMessagingService.queueMessage(ProtocolOutgoingMessages.START(sessionId, clientProfile.username));
                }else{
                    tcpMessagingService.queueMessage(ProtocolOutgoingMessages.UNREACHABLE(targetProfile.username));
                }
            }
        });

        chatServer.addTcpProtocol(ProtocolIncomingTcpMessages.HISTORY_REQ, new ContextualProtocol() {
            @Override
            public void executeProtocol() {
                TCPMessagingService tcpMessagingService = (TCPMessagingService) getContextValue(ContextValues.tcpMessagingService);
                ClientProfile clientProfile = (ClientProfile) getContextValue(ContextValues.clientProfile);
                tcpMessagingService.queueMessage(clientProfile.getHistory());
            }
        });

        chatServer.addTcpProtocol(ProtocolIncomingTcpMessages.END_REQUEST, new ContextualProtocol() {
            @Override
            public void executeProtocol() {
                // need to init this
                String partnerUsername = "";
                TCPMessagingService tcpMessagingService = (TCPMessagingService) getContextValue(ContextValues.tcpMessagingService);
                ClientProfile clientProfile = (ClientProfile) getContextValue(ContextValues.clientProfile);
                String sessionId = clientProfile.sessionId;
                TCPMessagingService targetMessagingService = chatServer.getTcpMessagingService(partnerUsername);
                tcpMessagingService.queueMessage(ProtocolOutgoingMessages.END_NOTIF(sessionId));
                targetMessagingService.queueMessage(ProtocolOutgoingMessages.END_NOTIF(sessionId));
            }
        });
    }

    public static void main(String[] args) {
        ChatServerLauncher chatServerLauncher = new ChatServerLauncher();
        try {
            chatServerLauncher.run(args);
        } catch (Exception ignored) {
            chatServerLauncher.printUsage();
        }
    }

}
