package edu.utdallas.cs5390.chat.framework.server;

import com.beust.jcommander.Parameter;

/**
 * Created by adisor on 11/20/2016.
 */
public class ChatServerArguments {

    @Parameter(names = "-port", required = true, description = "port to listen for incoming data")
    private int serverPort;

    @Parameter(names = "-file", required = true, description = "file in which user profiles are stored")
    private String usersFile;

    public int getServerPort() {
        return serverPort;
    }

    public String getUsersFile() {
        return usersFile;
    }
}
