package edu.utdallas.cs5390.chat.server;

import edu.utdallas.cs5390.chat.server.listeners.MessageListener;
import edu.utdallas.cs5390.chat.util.Utils;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Adisor on 10/1/2016.
 */

// should add 2 threads, one a transmitter, one a receiver
public class ClientService implements Runnable {
    private boolean stop;
    private Socket clientSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private ClientProfile clientProfile;
    private ClientService partner;
    private List<MessageListener> messageListeners;

    ClientService(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            clientSocket.setSoTimeout(100);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        messageListeners = new LinkedList<MessageListener>();
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientService getPartner() {
        return partner;
    }

    public void setPartner(ClientService partner) {
        this.partner = partner;
    }

    void addMessageListener(MessageListener messageListener) {
        messageListeners.add(messageListener);
    }

    public void sendMessage(String message) throws IOException {
        bufferedWriter.write(message);
    }

    @Override
    public void run() {
        while (!stop)
            processIncomingMessages();
        Utils.closeResource(clientSocket);
    }

    private void processIncomingMessages() {
        try {
            String message = bufferedReader.readLine();
            for (MessageListener listener : messageListeners)
                listener.processMessage(this, message);
        } catch (IOException ignored) {
        }
    }

    public void close() {
        stop = true;
        Utils.closeResource(bufferedReader);
        Utils.closeResource(bufferedWriter);
    }

}
