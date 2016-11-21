package edu.utdallas.cs5390.chat.framework.server;

import com.beust.jcommander.Parameter;

/**
 * Created by adisor on 11/20/2016.
 */
public class ChatServerArguments {

    @Parameter(names = "-tcpport", required = true, description = "port to listen for incoming data for tcp")
    private int tcpPort;

    @Parameter(names = "-udpport", required = true, description = "port to listen for incoming data for udp")
    private int udpPort;

    @Parameter(names = "-file", required = true, description = "file in which user profiles are stored")
    private String usersFile;

    public int getTcpPort() {
        return tcpPort;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public String getUsersFile() {
        return usersFile;
    }
}
