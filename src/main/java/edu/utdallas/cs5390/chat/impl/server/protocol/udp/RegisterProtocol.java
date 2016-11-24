package edu.utdallas.cs5390.chat.impl.server.protocol.udp;

import edu.utdallas.cs5390.chat.framework.common.ContextValues;
import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.connection.UdpConnection;
import edu.utdallas.cs5390.chat.framework.server.AbstractChatServer;
import edu.utdallas.cs5390.chat.framework.server.service.ClientProfile;
import edu.utdallas.cs5390.chat.impl.server.ProtocolOutgoingMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by adisor on 11/23/2016.
 */
public class RegisterProtocol extends ContextualProtocol {

    private Logger logger = LoggerFactory.getLogger(RegisterProtocol.class);

    private AbstractChatServer chatServer;

    public void setChatServer(AbstractChatServer chatServer) {
        this.chatServer = chatServer;
    }

    @Override
    public void executeProtocol() {
        try {
            logger.debug("Executing REGISTER protocol.");

            UdpConnection udpConnection = (UdpConnection) getContextValue(ContextValues.udpConnection);
            logger.debug("Retrieved udp connection from context values with ip: " + udpConnection.getDestinationAddress() + " and port " + udpConnection.getDestinationPort());

            ClientProfile clientProfile = chatServer.getProfileByAddress(udpConnection.getDestinationAddress() + ":" + udpConnection.getDestinationPort());
            clientProfile.isRegistered = true;
            logger.info(String.format("REGISTER udp request received from %s (address %s:%d, res:%s)",
                    clientProfile.username, udpConnection.getDestinationAddress(), udpConnection.getDestinationPort(),clientProfile.cipherKey));

            udpConnection.sendMessage(ProtocolOutgoingMessages.REGISTERED);
            logger.info("sending REGISTERED message to the client");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
