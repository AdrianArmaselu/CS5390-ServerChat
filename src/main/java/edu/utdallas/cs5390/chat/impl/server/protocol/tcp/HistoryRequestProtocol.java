package edu.utdallas.cs5390.chat.impl.server.protocol.tcp;

import edu.utdallas.cs5390.chat.framework.common.ContextValues;
import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.service.TCPMessagingService;
import edu.utdallas.cs5390.chat.framework.server.service.ClientProfile;

/**
 * Created by adisor on 11/22/2016.
 */
public class HistoryRequestProtocol extends ContextualProtocol{

    public HistoryRequestProtocol() {
        super();
    }

    @Override
    public void executeProtocol() { // TODO: I AM SUPPOSED TO USE THE SESSION ID LOL
        TCPMessagingService tcpMessagingService = (TCPMessagingService) getContextValue(ContextValues.tcpMessagingService);
        ClientProfile clientProfile = (ClientProfile) getContextValue(ContextValues.clientProfile);
        tcpMessagingService.queueMessage(clientProfile.getHistory());
    }
}
