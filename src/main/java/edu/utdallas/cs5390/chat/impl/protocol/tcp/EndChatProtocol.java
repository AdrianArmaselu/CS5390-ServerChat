package edu.utdallas.cs5390.chat.impl.protocol.tcp;

import edu.utdallas.cs5390.chat.impl.messages.ProtocolServerResponses;
import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;

/**
 * Created by adisor on 10/30/2016.
 */
public class EndChatProtocol extends ContextualProtocol{
    @Override
    public void executeProtocol(){
        System.out.println(isChatEnded() ? "Chat Ended" : "User unreachable");
    }

    private boolean isChatEnded(){
        return getContextValue("message").equals(ProtocolServerResponses.CHAT_ENDED);
    }
}
