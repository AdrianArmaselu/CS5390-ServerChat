package edu.utdallas.cs5390.chat.client;

import edu.utdallas.cs5390.chat.util.Utils;

import java.io.*;
import java.net.Socket;

/**
 * Created by Adisor on 10/1/2016.
 */

public class TCPClientConnection extends AbstractClientConnection {
    private final Socket socket;
    private final BufferedReader bufferedReader;
    private final BufferedWriter bufferedWriter;

    public TCPClientConnection(String host, int port) throws IOException {
        socket = new Socket(host, port);
        socket.setSoTimeout(RECEIVE_TIMEOUT);
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void sendMessage(String message) throws IOException {
        bufferedWriter.write(message);
    }

    public String receiveMessage() throws IOException {
        return bufferedReader.readLine();
    }

    public void close() throws IOException {
        Utils.closeResource(socket);
    }
}
