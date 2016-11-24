package edu.utdallas.cs5390.chat.impl.server.protocol.tcp;

import edu.utdallas.cs5390.chat.framework.common.ContextValues;
import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.service.TCPMessagingService;
import edu.utdallas.cs5390.chat.framework.common.util.Utils;
import edu.utdallas.cs5390.chat.framework.server.AbstractChatServer;
import edu.utdallas.cs5390.chat.framework.server.service.ClientProfile;
import edu.utdallas.cs5390.chat.impl.server.ProtocolOutgoingMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by adisor on 11/23/2016.
 */
public class ConnectProtocol extends ContextualProtocol {
    private Logger logger = LoggerFactory.getLogger(ConnectProtocol.class);

    public ConnectProtocol() {
        super();
    }
    @Override
    public void executeProtocol() {
        TCPMessagingService tcpMessagingService = (TCPMessagingService) getContextValue(ContextValues.tcpMessagingService);
        ClientProfile clientProfile = (ClientProfile) getContextValue(ContextValues.clientProfile);
        String targetUsername = Utils.extractValue((String) getContextValue(ContextValues.message));
        AbstractChatServer chatServer = (AbstractChatServer) getContextValue(ContextValues.chatServer);
        ClientProfile targetProfile = chatServer.getProfileByUsername(targetUsername);
        clientProfile.partner = targetUsername;
        targetProfile.partner = clientProfile.username;
        logger.info("Received connect request. from " + clientProfile.username + " to " + targetUsername);
        if (targetProfile.isRegistered && !targetProfile.inSession) {
            String sessionId = Utils.UUID();
            TCPMessagingService targetMessagingService = chatServer.getTcpMessagingService(targetUsername);
            targetMessagingService.partner = clientProfile.username;
            tcpMessagingService.partner = targetUsername;
            clientProfile.sessionId = sessionId;
            targetProfile.sessionId = sessionId;
            tcpMessagingService.queueMessage(ProtocolOutgoingMessages.START(sessionId, targetUsername));
            targetMessagingService.queueMessage(ProtocolOutgoingMessages.START(sessionId, clientProfile.username));
        } else {
            tcpMessagingService.queueMessage(ProtocolOutgoingMessages.UNREACHABLE(targetProfile.username));
        }
    }
}
