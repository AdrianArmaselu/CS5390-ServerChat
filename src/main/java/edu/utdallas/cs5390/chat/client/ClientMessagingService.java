package edu.utdallas.cs5390.chat.client;

import edu.utdallas.cs5390.chat.client.connection.EncryptedTCPClientConnection;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by adisor on 10/30/2016.
 */
public class ClientMessagingService extends Thread {
    private EncryptedTCPClientConnection clientConnection;
    private BlockingQueue<String> outgoingMessages;
    private boolean stop;

    public ClientMessagingService(AbstractChatClient chatClient) {
        clientConnection = chatClient.getTcpConnection();
        outgoingMessages = new LinkedBlockingQueue<>();
    }

    public void run() {
        while (!isInterrupted()) {
            sendMessages();
            printReceivedMessages();
        }
        stop = true;
    }

    public void addMessage(String message){
        outgoingMessages.add(message);
    }

    private void sendMessages() {
        while (!outgoingMessages.isEmpty())
            sendMessage();
    }

    void sendMessage() {
        try {
            clientConnection.sendMessage(outgoingMessages.poll());
        } catch (IOException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void printReceivedMessages(){
        while(printReceivedMessage() != null);
    }

    private String printReceivedMessage(){
        String receivedMessage = null;
        try {
            receivedMessage = clientConnection.receiveMessage();
            System.out.println(receivedMessage);
        } catch (IOException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return receivedMessage;
    }

    public void shutdown(){
        interrupt();
        while(!stop){
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
