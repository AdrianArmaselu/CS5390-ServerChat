package edu.utdallas.cs5390.chat.client.protocol;

import edu.utdallas.cs5390.chat.client.AbstractChatClient;
import edu.utdallas.cs5390.chat.client.protocol.messages.ProtocolInputCommands;
import edu.utdallas.cs5390.chat.client.protocol.tcp.EndChatProtocol;
import edu.utdallas.cs5390.chat.client.protocol.tcp.StartChatProtocol;
import edu.utdallas.cs5390.chat.client.protocol.udp.LogonProtocol;
import edu.utdallas.cs5390.chat.common.ContextualProtocol;

/**
 * Created by adisor on 11/16/2016.
 */
public class CommandsProtocol extends ContextualProtocol {

    private final AbstractChatClient chatClient;

    public CommandsProtocol(AbstractChatClient chatClient) {
        this.chatClient = chatClient;
        chatClient.addTCPProtocol(new EndChatProtocol());
        chatClient.addTCPProtocol(new StartChatProtocol());
    }

    @Override
    public void executeProtocol() {
        String message = (String) getContextValue("cli.message");
        switch (message) {
            case ProtocolInputCommands.LOGON:
                try {
                    new LogonProtocol(chatClient).executeProtocol();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case ProtocolInputCommands.START_CHAT:
                if (!chatClient.isInChatSession())
                    startChat();
                else
                    System.out.println("Sorry bud, you are already in a chat session. First disconnect");
                break;
            case ProtocolInputCommands.END_CHAT:
                try {
                    chatClient.queueMessage("end");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case ProtocolInputCommands.LOGOFF:
                chatClient.logoff();
                break;
            case ProtocolInputCommands.EXIT:
                chatClient.shutdown();
                break;
            default:
                if (chatClient.isInChatSession())
                    chatClient.queueMessage(message);
                else
                    System.out.println("Command Not Recognized. Here is a list of available commands: <Needs development>");
        }
    }

    private void startChat() {
        String chatPartnerUsername = chatClient.readInput();
        chatClient.queueMessage("connect: " + chatPartnerUsername);
        try {
            chatClient.setIsInChatSession(true);
        } catch (Exception e) {
            e.printStackTrace();
            chatClient.setIsInChatSession(false);
        }
    }
}
