package edu.utdallas.cs5390.chat.impl.server.protocol.tcp;

import edu.utdallas.cs5390.chat.framework.common.ContextValues;
import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.service.TCPMessagingService;
import edu.utdallas.cs5390.chat.framework.server.AbstractChatServer;
import edu.utdallas.cs5390.chat.framework.server.service.ClientProfile;
import edu.utdallas.cs5390.chat.impl.server.ProtocolOutgoingMessages;

/**
 * Created by adisor on 11/23/2016.
 */
public class EndRequestProtocol extends ContextualProtocol {

    public EndRequestProtocol() {
    }

    @Override
    public void executeProtocol() {
        // need to init this
        TCPMessagingService tcpMessagingService = (TCPMessagingService) getContextValue(ContextValues.tcpMessagingService);
        ClientProfile clientProfile = (ClientProfile) getContextValue(ContextValues.clientProfile);
        AbstractChatServer chatServer = (AbstractChatServer) getContextValue(ContextValues.chatServer);
        String sessionId = clientProfile.sessionId;
        TCPMessagingService targetMessagingService = chatServer.getTcpMessagingService(clientProfile.partner);
        tcpMessagingService.queueMessage(ProtocolOutgoingMessages.END_NOTIF(sessionId));
        targetMessagingService.queueMessage(ProtocolOutgoingMessages.END_NOTIF(sessionId));
    }
}
