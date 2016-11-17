package edu.utdallas.cs5390.chat.framework.client;

import com.beust.jcommander.Parameter;

/**
 * Created by adisor on 11/1/2016.
 */
public class ChatClientArguments {

    @Parameter(names = "-host", required = true, description = "server host address")
    private String serverAddress;

    @Parameter(names = "-port", required = true, description = "port to listen for incoming data")
    private int serverPort;

    @Parameter(names = "-user", required = true, description = "username/userid")
    private String username;

    @Parameter(names = "-pass", required = true, description = "user password")
    private String password;

    public String getServerAddress() {
        return serverAddress;
    }

    public int getPort() {
        return serverPort;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
