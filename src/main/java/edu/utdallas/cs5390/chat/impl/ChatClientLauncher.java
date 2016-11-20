package edu.utdallas.cs5390.chat.impl;

import com.beust.jcommander.JCommander;
import edu.utdallas.cs5390.chat.framework.client.AbstractChatClient;
import edu.utdallas.cs5390.chat.framework.client.ChatClient;
import edu.utdallas.cs5390.chat.framework.client.ChatClientArguments;
import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;
import edu.utdallas.cs5390.chat.impl.messages.ProtocolInputCommands;
import edu.utdallas.cs5390.chat.impl.messages.ProtocolServerRequests;
import edu.utdallas.cs5390.chat.impl.messages.ProtocolServerResponses;
import edu.utdallas.cs5390.chat.impl.protocol.udp.LogonProtocol;

import java.io.IOException;

/**
 * Created by aarmaselu on 11/17/2016.
 */
public class ChatClientLauncher {

    private AbstractChatClient chatClient;

    private void run(String[] args) throws IOException {
        ChatClientArguments chatClientArguments = new ChatClientArguments();
        JCommander jCommander = new JCommander(chatClientArguments);
        jCommander.parse(args);
        chatClient = new ChatClient(chatClientArguments);
        addCLIProtocols();
        addTCPProtocols();
        chatClient.run();
    }

    private void printUsage(){
        new JCommander(new ChatClientArguments()).usage();
    }

    private void addTCPProtocols(){
        chatClient.addTCPProtocol(ProtocolServerResponses.END_NOTIF, new ContextualProtocol() {
            @Override
            public void executeProtocol() {
                try {
                    chatClient.setIsInChatSession(false);
                    chatClient.setPartnerUsername(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        chatClient.addTCPProtocol(ProtocolServerResponses.START, new ContextualProtocol() {
            @Override
            public void executeProtocol() {
                String response = (String) getContextValue("response");
                String chatPartnerUsername = ProtocolServerResponses.extractValue(response);
                chatClient.setPartnerUsername(chatPartnerUsername);
                chatClient.setIsInChatSession(true);
            }
        });
        chatClient.addTCPProtocol(ProtocolServerResponses.HISTORY_RESP, new ContextualProtocol() {
            @Override
            public void executeProtocol() {
                String response = (String) getContextValue("response");
                String history = ProtocolServerResponses.extractValue(response);
                System.out.println(history);
            }
        });
    }

    private void addCLIProtocols(){
        chatClient.addCliProtocol(ProtocolInputCommands.LOGON, new LogonProtocol(chatClient));
        chatClient.addCliProtocol(ProtocolInputCommands.START_CHAT, new ContextualProtocol() {
            @Override
            public void executeProtocol() {
                if (!chatClient.isInChatSession()) {
                    String chatPartnerUsername = chatClient.readInput();
                    chatClient.setPartnerUsername(chatPartnerUsername);
                    chatClient.queueMessage("connect: " + chatPartnerUsername);
                    chatClient.setIsInChatSession(true);
                } else {
                    chatClient.setIsInChatSession(false);
                    System.out.println("Sorry bud, you are already in a chat session. First disconnect");
                }
            }
        });
        chatClient.addCliProtocol(ProtocolInputCommands.END_CHAT, new ContextualProtocol() {
            @Override
            public void executeProtocol() {
                try {
                    chatClient.queueMessage("end");
                    chatClient.setIsInChatSession(false);
                    chatClient.setPartnerUsername(null);
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
    }

    public static void main(String[] args) {
        ChatClientLauncher chatClientLauncher = new ChatClientLauncher();
        try {
            chatClientLauncher.run(args);
        } catch (Exception ignored) {
            chatClientLauncher.printUsage();
        }
    }

}
