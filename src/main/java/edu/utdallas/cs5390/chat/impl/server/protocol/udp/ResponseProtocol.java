package edu.utdallas.cs5390.chat.impl.server.protocol.udp;

import edu.utdallas.cs5390.chat.framework.common.ContextValues;
import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.connection.UdpConnection;
import edu.utdallas.cs5390.chat.framework.common.util.Utils;
import edu.utdallas.cs5390.chat.framework.server.AbstractChatServer;
import edu.utdallas.cs5390.chat.framework.server.service.ClientProfile;
import edu.utdallas.cs5390.chat.impl.server.ProtocolOutgoingMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by adisor on 11/23/2016.
 */
public class ResponseProtocol extends ContextualProtocol {
    private Logger logger = LoggerFactory.getLogger(ResponseProtocol.class);

    private AbstractChatServer chatServer;

    public void setChatServer(AbstractChatServer chatServer) {
        this.chatServer = chatServer;
    }

    @Override
    public void executeProtocol() {
        try {
            logger.debug("Executing RESPONSE protocol.");

            UdpConnection udpConnection = (UdpConnection) getContextValue(ContextValues.udpConnection);
            logger.debug("Retrieved udp connection from context values with ip: " + udpConnection.getDestinationAddress() + " and port " + udpConnection.getDestinationPort());

            ClientProfile clientProfile = chatServer.getProfileByAddress(udpConnection.getDestinationAddress() + ":" + udpConnection.getDestinationPort());
            String clientRes = Utils.extractValue((String) getContextValue(ContextValues.message));
            boolean authenticationCodesMatch = clientRes.equals(clientProfile.cipherKey);
            logger.info(String.format("REGISTER udp request received from %s (address %s:%d, res:%s, received.res:%s)",
                    clientProfile.username, udpConnection.getDestinationAddress(), udpConnection.getDestinationPort(),clientProfile.cipherKey, clientRes));

            if (authenticationCodesMatch) {

                udpConnection.addEncryption(clientProfile.encryptionKey);
                logger.info("Client res matches with the server res");

                udpConnection.sendMessage(ProtocolOutgoingMessages.AUTH_SUCCESS);
                logger.info("sending AUTH_SUCCESS message to the client");

            } else {
                udpConnection.sendMessage(ProtocolOutgoingMessages.AUTH_FAIL);
                logger.info("sending AUTH_FAIL message to the client");
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
