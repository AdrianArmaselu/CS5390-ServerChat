package edu.utdallas.cs5390.chat;

import edu.utdallas.cs5390.chat.client.ChatClient;
import edu.utdallas.cs5390.chat.server.ChatServer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Adisor on 10/1/2016.
 */
public class BaseTest {

    public void testSendMessageAndReceiveResponse(){
        ChatServer chatServer = new ChatServer();
        ChatClient chatClient = new ChatClient();

    }

    public static void main(String[] args) throws InterruptedException {
        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    System.in.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        while(true){
            System.out.println(executorService.getActiveCount());
            Thread.sleep(1000);
        }
    }

}
