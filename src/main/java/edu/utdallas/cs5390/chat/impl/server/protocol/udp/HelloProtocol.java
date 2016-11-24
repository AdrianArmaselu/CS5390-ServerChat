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
public class HelloProtocol extends ContextualProtocol {

    private Logger logger = LoggerFactory.getLogger(HelloProtocol.class);

    private AbstractChatServer chatServer;

    public void setChatServer(AbstractChatServer chatServer) {
        this.chatServer = chatServer;
    }

    @Override
    public void executeProtocol() {
        try {
            logger.debug("Executing HELLO protocol.");

            UdpConnection udpConnection = (UdpConnection) getContextValue(ContextValues.udpConnection);
            logger.debug("Retrieved udp connection from context values with ip: " + udpConnection.getDestinationAddress() + " and port " + udpConnection.getDestinationPort());

            String username = Utils.extractValue((String) getContextValue(ContextValues.message));
            logger.debug("Retrieved username " + username + " from context values");

            if (chatServer.isASubscriber(username)) {
                logger.debug("User is a subscriber");

                ClientProfile clientProfile = chatServer.getProfileByUsername(username);
                logger.debug("client profile retrieved from the chat server based on username");

                chatServer.addAddressProfile(udpConnection.getDestinationAddress() + ":" + udpConnection.getDestinationPort(), clientProfile);
                logger.debug("added an address mapping to the client profile " + udpConnection.getDestinationAddress() + ":" + udpConnection.getDestinationPort());

                // setup encryption
                String rand = Utils.randomString(4);
                logger.debug("created random string " + rand);

                clientProfile.cipherKey = Utils.createCipherKey(rand, clientProfile.password);
                logger.debug("created cipher key " + clientProfile.cipherKey);

                clientProfile.encryptionKey = Utils.createEncryptionKey(clientProfile.cipherKey);
                logger.debug("created encryption key");

                logger.info(String.format("HELLO udp request received from %s (address %s:%d, rand:%s, res:%s)",
                        username, udpConnection.getDestinationAddress(), udpConnection.getDestinationPort(),
                        rand, clientProfile.cipherKey));
                // send challenge
                udpConnection.sendMessage(ProtocolOutgoingMessages.CHALLENGE(rand));
                logger.info("sending challenge message to the client");

            } else
                logger.info(String.format("HELLO udp request received from unregistered %s (address %s:%d)",
                        username, udpConnection.getDestinationAddress(), udpConnection.getDestinationPort()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
