package edu.utdallas.cs5390.chat.impl.client;

import com.beust.jcommander.JCommander;
import edu.utdallas.cs5390.chat.framework.client.AbstractChatClient;
import edu.utdallas.cs5390.chat.framework.client.ChatClient;
import edu.utdallas.cs5390.chat.framework.client.ChatClientArguments;
import edu.utdallas.cs5390.chat.framework.common.ContextValues;
import edu.utdallas.cs5390.chat.framework.common.ContextualProtocol;

import java.io.IOException;
import java.util.List;

/**
 * Created by aarmaselu on 11/17/2016.
 */
//TODO: WHEN LAUNCHING A NEW CLIENT, CHECK TO SEE IF UDP PORT FOR LISTENING IS BOUND. IF IT IS, USE THE NEXT PORT. SERVER NEEDS TO KEEP TRACK OF THIS
class ChatClientLauncher {

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
                    chatClient.setSessionId(null);
                    chatClient.setPartnerUsername(null);
                    System.out.println("Chat session ended");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        chatClient.addTCPProtocol(ProtocolServerResponses.START, new ContextualProtocol() {
            @Override
            public void executeProtocol() {
                String response = (String) getContextValue(ContextValues.message);
                List<String> values = ProtocolServerResponses.extractValues(response);
                chatClient.setPartnerUsername(values.get(1));
                chatClient.setSessionId(values.get(0));
                System.out.println("Started chat session with user " + values.get(1) + ". Session id is " + values.get(0));
            }
        });
        chatClient.addTCPProtocol(ProtocolServerResponses.HISTORY_RESP, new ContextualProtocol() {
            @Override
            public void executeProtocol() {
                String response = (String) getContextValue(ContextValues.message);
                String history = ProtocolServerResponses.extractValue(response);
                System.out.println("History: ");
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
                    System.out.println("Sending start chat request to the server...");
                    String chatPartnerUsername = (String) getContextValue(ContextValues.chatPartnerUsername);
                    chatClient.setPartnerUsername(chatPartnerUsername);
                    chatClient.queueMessage(ProtocolServerRequests.CONNECT(chatPartnerUsername));
                } else {
                    System.out.println("You are already in a chat session. Disconnect first.");
                }
            }
        });
        chatClient.addCliProtocol(ProtocolInputCommands.END_CHAT, new ContextualProtocol() {
            @Override
            public void executeProtocol() {
                try {
                    System.out.println("Sending end chat request to the server...");
                    chatClient.queueMessage(ProtocolServerRequests.END_REQUEST(chatClient.getSessionId()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        chatClient.addCliProtocol(ProtocolInputCommands.LOGOFF, new ContextualProtocol() {
            @Override
            public void executeProtocol() {
                System.out.println("Logging off...");
                chatClient.logoff();
                System.out.println("Logged off");
            }
        });
        chatClient.addCliProtocol(ProtocolInputCommands.EXIT, new ContextualProtocol() {
            @Override
            public void executeProtocol() {
                System.out.println("Shutting down...");
                chatClient.shutdown();
            }
        });
        chatClient.addCliProtocol(ProtocolInputCommands.HISTORY, new ContextualProtocol() {
            @Override
            public void executeProtocol() {
                System.out.println("Sending history request to the server...");
                chatClient.queueMessage(ProtocolServerRequests.HISTORY_REQ(chatClient.getPartnerUsername()));
            }
        });
    }

    public static void main(String[] args) {
        ChatClientLauncher chatClientLauncher = new ChatClientLauncher();
        try {
            chatClientLauncher.run(args);
        } catch (Exception ignored) {
            ignored.printStackTrace();
            chatClientLauncher.printUsage();
        }
    }

}
