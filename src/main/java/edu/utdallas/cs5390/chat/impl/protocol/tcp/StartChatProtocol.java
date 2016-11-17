package edu.utdallas.cs5390.chat.impl.protocol.tcp;

import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;

/**
 * Created by adisor on 10/30/2016.
 */
public class StartChatProtocol extends ContextualProtocol {
    @Override
    public void executeProtocol() {
        System.out.println(isChatStarted() ? "Chat started" : "User unreachable");
    }

    private boolean isChatStarted() {
        return getContextValue("message").equals("setup");
    }
}
