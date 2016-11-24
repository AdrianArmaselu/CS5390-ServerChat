package edu.utdallas.cs5390.chat.impl.server;

import com.beust.jcommander.JCommander;
import edu.utdallas.cs5390.chat.framework.common.util.CLIReader;
import edu.utdallas.cs5390.chat.framework.server.AbstractChatServer;
import edu.utdallas.cs5390.chat.framework.server.ChatServer;
import edu.utdallas.cs5390.chat.framework.server.ChatServerArguments;
import edu.utdallas.cs5390.chat.impl.server.protocol.tcp.ConnectProtocol;
import edu.utdallas.cs5390.chat.impl.server.protocol.tcp.EndRequestProtocol;
import edu.utdallas.cs5390.chat.impl.server.protocol.tcp.HistoryRequestProtocol;
import edu.utdallas.cs5390.chat.impl.server.protocol.udp.HelloProtocol;
import edu.utdallas.cs5390.chat.impl.server.protocol.udp.RegisterProtocol;
import edu.utdallas.cs5390.chat.impl.server.protocol.udp.ResponseProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by adisor on 11/20/2016.
 */

// TODO: IF A MESSAGE NEEDS TO BE RETRANSMITTED, DO NOT RE-EXECUTE THE CODE
// TODO : DEBUG FOR MULTIPLE CLIENTS CONNECTED
    // TODO: CHANGE THE WAY THE ENCRYPTION KEY IS CREATED, DO NOT TRANSPORT IT
    // todo: handle people that try to login again with same credentials
    // TODO: PROTOCOL EXECUTIONS ARE NOT ROBUST
    //TRY CONNECTING TWO USERS AT THE SAME TIME
//8080 - udp server, 8081 - tcp
class ChatServerLauncher {
    private final Logger logger = LoggerFactory.getLogger(ChatServerLauncher.class);
    private AbstractChatServer chatServer;

    private void run(String[] args) throws IOException {

        logger.info("Starting Server... ");

        ChatServerArguments chatServerArguments = new ChatServerArguments();
        JCommander jCommander = new JCommander(chatServerArguments);
        jCommander.parse(args);
        logger.info("Parsed Arguments");

        chatServer = new ChatServer(chatServerArguments);

        addCLIProtocols();
        addUdpProtocols();
        addTCPProtocols();

        //setup
        CLIReader cliReader = new CLIReader();
        String command = "";
        // startup
        chatServer.startup();
        logger.info("Server Started");

        while (!command.equals("exit")) {
            command = cliReader.readInput();
        }

        logger.info("Server is shutting down...");

        chatServer.shutdown();
        logger.info("Server shutdown");

        System.exit(0);
    }


    private void printUsage() {
        new JCommander(new ChatServerArguments()).usage();
    }

    private void addCLIProtocols() {
        logger.info("Added Command Line Protocols");
    }

    private void addUdpProtocols() {
        HelloProtocol helloProtocol = new HelloProtocol();
        helloProtocol.setChatServer(chatServer);
        ResponseProtocol responseProtocol = new ResponseProtocol();
        responseProtocol.setChatServer(chatServer);
        RegisterProtocol registerProtocol = new RegisterProtocol();
        registerProtocol.setChatServer(chatServer);
        chatServer.addUdpProtocol(ProtocolIncomingUdpMessages.HELLO, helloProtocol);
        chatServer.addUdpProtocol(ProtocolIncomingUdpMessages.RESPONSE, responseProtocol);
        chatServer.addUdpProtocol(ProtocolIncomingUdpMessages.REGISTER, registerProtocol);
        logger.info("Added Udp Protocols");
    }

    private void addTCPProtocols() {
        chatServer.addTcpProtocol(ProtocolIncomingTcpMessages.CONNECT, new ConnectProtocol());
        chatServer.addTcpProtocol(ProtocolIncomingTcpMessages.HISTORY_REQ, new HistoryRequestProtocol());
        chatServer.addTcpProtocol(ProtocolIncomingTcpMessages.END_REQUEST, new EndRequestProtocol());
        logger.info("Added Tcp Protocols");
    }

    public static void main(String[] args) {
        ChatServerLauncher chatServerLauncher = new ChatServerLauncher();
        try {
            chatServerLauncher.run(args);
        } catch (Exception ignored) {
            chatServerLauncher.printUsage();
        }
    }
}
