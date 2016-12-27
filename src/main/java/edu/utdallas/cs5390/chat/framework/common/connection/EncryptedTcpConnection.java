package edu.utdallas.cs5390.chat.framework.common.connection;

import edu.utdallas.cs5390.chat.framework.common.ChatPacket;
import edu.utdallas.cs5390.chat.framework.common.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Adisor on 10/1/2016.
 */

public class EncryptedTcpConnection extends AbstractConnection {
    private final Logger logger = LoggerFactory.getLogger(EncryptedTcpConnection.class);
    private final Socket socket;
    private final BufferedInputStream bufferedInputStream;
    private final BufferedOutputStream bufferedOutputStream;
    private Key encryptionKey;
    private Key senderEncryptionKey;
    private Key receiverEncryptionKey;

    public EncryptedTcpConnection(Socket socket, Key encryptionKey) throws IOException {
        logger.debug("Creating encrypted tcp connection with destination address " + socket);
        this.socket = socket;
        socket.setSoTimeout(RECEIVE_TIMEOUT);
        bufferedInputStream = new BufferedInputStream(socket.getInputStream());
        bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
        this.encryptionKey = encryptionKey;
        senderEncryptionKey = encryptionKey;
        receiverEncryptionKey = encryptionKey;
        logger.debug("Encrypted tcp connection created.");
    }

    public void sendMessage(String message) throws IOException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        logger.debug("Sending message " + message);
        logger.debug("Packaging the message into a chat package...");
        ChatPacket chatPacket = new ChatPacket(message, senderEncryptionKey);
        logger.debug("Writing the chat packet bytes to the stream...");
        bufferedOutputStream.write(chatPacket.getData());
        bufferedOutputStream.flush();
        logger.debug("Chat packet bytes transmitted");
    }

    public String receiveMessage() throws IOException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] buffer = new byte[1024];
        bufferedInputStream.read(buffer);
        ChatPacket chatPacket = new ChatPacket(buffer);
        String message = chatPacket.getMessage(receiverEncryptionKey);
        logger.info("Received message " + message);
        return message;
    }

    public void close() {
        logger.info("Closing socket...");
        Utils.closeResource(socket);
        logger.info("Connection Closed");
    }

    @Override
    public String getIpAddress() {
        return socket.getInetAddress().getHostAddress();
    }

    public String getDestinationAddress() {
        return getIpAddress() + ":" + socket.getPort();
    }

    public void enableEncryption() {
        senderEncryptionKey = encryptionKey;
        enableReceiverEncryption();
    }

    public void disableEncryption() {
        senderEncryptionKey = null;
        receiverEncryptionKey = null;
    }

    public void setEncryptionKey(Key encryptionKey) {
        this.encryptionKey = encryptionKey;
        enableEncryption();
    }

    public void enableReceiverEncryption() {
        receiverEncryptionKey = encryptionKey;
    }
}
