package edu.utdallas.cs5390.chat.client;

import edu.utdallas.cs5390.chat.CLIReader;
import edu.utdallas.cs5390.chat.util.TransmissionException;

import java.io.IOException;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Adisor on 10/1/2016.
 */

// TODO: LOOK INTO HASING AND HEX STRING
// TODO: MODULARIZE TRANSMISSION
public class ChatClient {
    private static final String HOST_ADDRESS = "127.0.0.1";
    private static final int HOST_PORT = 8080;
    private UDPClientConnection udpConnection;
    private TCPClientConnection tcpConnection;
    private CLIReader cliReader;
    private MessageDigest authenticationAlgorithm;

    private String username;
    private String password;

    public ChatClient() {
        try {
            udpConnection = new UDPClientConnection(HOST_ADDRESS, HOST_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        try {
            tcpConnection = new TCPClientConnection(HOST_ADDRESS, HOST_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            authenticationAlgorithm = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        cliReader = new CLIReader();

    }

    public void shutdown() {
        udpConnection.close();
        try {
            tcpConnection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cliReader.close();
    }

    public void run() {
        String command = "";
        while (!command.equals("EXIT")) {
            command = cliReader.getNextCommand();
            if (command.equals("Log on"))
                try {
                    String response = udpConnection.sendMessageAndGetResponse("hello");
                } catch (TransmissionException e) {
                    e.printStackTrace();
                }
        }
        shutdown();
    }

    private String hash(String random) {
        String finalString = random + password;
        String hash = new String(authenticationAlgorithm.digest(finalString.getBytes()));
        return hash;
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.run();
    }

}
