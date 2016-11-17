package edu.utdallas.cs5390.chat.impl;

import com.beust.jcommander.JCommander;
import edu.utdallas.cs5390.chat.framework.client.AbstractChatClient;
import edu.utdallas.cs5390.chat.framework.client.ChatClient;
import edu.utdallas.cs5390.chat.framework.client.ChatClientArguments;
import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.impl.messages.ProtocolInputCommands;
import edu.utdallas.cs5390.chat.impl.messages.ProtocolServerRequests;
import edu.utdallas.cs5390.chat.impl.protocol.tcp.EndChatProtocol;
import edu.utdallas.cs5390.chat.impl.protocol.tcp.StartChatProtocol;
import edu.utdallas.cs5390.chat.impl.protocol.udp.LogonProtocol;

/**
 * Created by aarmaselu on 11/17/2016.
 */
public class ChatClientLauncher {

    public static void main(String[] args) {
        ChatClientArguments chatClientArguments = new ChatClientArguments();
        JCommander jCommander = new JCommander(chatClientArguments);
        try {
            jCommander.parse(args);
            final AbstractChatClient chatClient = new ChatClient(chatClientArguments);
            chatClient.addTCPProtocol(new EndChatProtocol());
            chatClient.addTCPProtocol(new StartChatProtocol());
            chatClient.addTCPProtocol(new ContextualProtocol() {
                @Override
                public void executeProtocol() {

                }
            });
            chatClient.addCliProtocol(ProtocolInputCommands.LOGON, new LogonProtocol(chatClient));
            chatClient.addCliProtocol(ProtocolInputCommands.START_CHAT, new ContextualProtocol() {
                @Override
                public void executeProtocol() {
                    if (!chatClient.isInChatSession()) {
                        String chatPartnerUsername = chatClient.readInput();
                        chatClient.setPartnerUsername(chatPartnerUsername);
                        chatClient.queueMessage("connect: " + chatPartnerUsername);
                        try {
                            chatClient.setIsInChatSession(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                            chatClient.setIsInChatSession(false);
                        }
                    } else {
                        System.out.println("Sorry bud, you are already in a chat session. First disconnect");
                    }
                }
            });
            chatClient.addCliProtocol(ProtocolInputCommands.END_CHAT, new ContextualProtocol() {
                @Override
                public void executeProtocol() {
                    try {
                        chatClient.queueMessage("end");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            chatClient.addCliProtocol(ProtocolInputCommands.LOGOFF, new ContextualProtocol() {
                @Override
                public void executeProtocol() {
                    chatClient.logoff();
                }
            });
            chatClient.addCliProtocol(ProtocolInputCommands.EXIT, new ContextualProtocol() {
                @Override
                public void executeProtocol() {
                    chatClient.shutdown();
                }
            });
            chatClient.addCliProtocol(ProtocolInputCommands.HISTORY, new ContextualProtocol() {
                @Override
                public void executeProtocol() {
                    chatClient.queueMessage(ProtocolServerRequests.HISTORY_REQ(chatClient.getPartnerUsername()));
                }
            });
            chatClient.run();
        } catch (Exception ignored) {
            jCommander.usage();
        }
    }
}
