package edu.utdallas.cs5390.chat.impl.server;

import edu.utdallas.cs5390.chat.framework.common.ContextValues;
import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.framework.common.service.TCPMessagingService;
import edu.utdallas.cs5390.chat.framework.server.service.ClientProfile;
import edu.utdallas.cs5390.chat.impl.server.protocol.tcp.HistoryRequestProtocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by adisor on 11/22/2016.
 */
public class Test {

    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        Test test = new Test();
        test.test();

    }
    private void test() throws IllegalAccessException, InstantiationException {
        Map<String, ContextualProtocol> tcpProtocols = new HashMap<>();
        tcpProtocols.put("lala", new ContextualProtocol() {
            @Override
            public void executeProtocol() {
                TCPMessagingService tcpMessagingService = (TCPMessagingService) getContextValue(ContextValues.tcpMessagingService);
                ClientProfile clientProfile = (ClientProfile) getContextValue(ContextValues.clientProfile);
                tcpMessagingService.queueMessage(clientProfile.getHistory());
            }
        });
       ContextualProtocol contextualProtocol = new ContextualProtocol() {
            @Override
            public void executeProtocol() {
                TCPMessagingService tcpMessagingService = (TCPMessagingService) getContextValue(ContextValues.tcpMessagingService);
                ClientProfile clientProfile = (ClientProfile) getContextValue(ContextValues.clientProfile);
                tcpMessagingService.queueMessage(clientProfile.getHistory());
            }
        };
        contextualProtocol = new HistoryRequestProtocol();
        contextualProtocol.getClass().newInstance();
//        tcpProtocols.get("lala").getClass().newInstance();
    }
}
