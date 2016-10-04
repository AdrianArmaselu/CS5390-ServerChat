package edu.utdallas.cs5390.chat.server.service;

import edu.utdallas.cs5390.chat.server.processor.MessageProcessor;
import edu.utdallas.cs5390.chat.util.Utils;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Adisor on 10/1/2016.
 */

// should move socket away from here
public class ClientMessagingService implements Runnable {
    private boolean stop;
    private Socket clientSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private List<MessageProcessor> messageListeners;

    public ClientMessagingService(Socket clientSocket) throws IOException {
        messageListeners = new LinkedList<MessageProcessor>();
        this.clientSocket = clientSocket;
        clientSocket.setSoTimeout(100);
        bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
    }

    public void addMessageProcessor(MessageProcessor MessageProcessor) {
        messageListeners.add(MessageProcessor);
    }

    @Override
    public void run() {
        while (!stop)
            receiveIncomingMessages();
        Utils.closeResource(clientSocket);
    }

    private void receiveIncomingMessages() {
        try {
            String message = bufferedReader.readLine();
            processMessage(message);
        } catch (IOException ignored) {
        }
    }

    private void processMessage(String message){
        for (MessageProcessor listener : messageListeners)
            listener.processMessage(message);
    }

    public void sendMessage(String message) throws IOException {
        bufferedWriter.write(message);
    }

    public void close() {
        stop = true;
        Utils.closeResource(bufferedReader);
        Utils.closeResource(bufferedWriter);
    }
}
