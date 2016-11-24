package edu.utdallas.cs5390.chat.framework.common;

import edu.utdallas.cs5390.chat.framework.common.connection.AbstractConnection;
import edu.utdallas.cs5390.chat.framework.common.util.Utils;

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
    private final Socket socket;
    private final BufferedInputStream bufferedInputStream;
    private final BufferedOutputStream bufferedOutputStream;
    private final Key encryptionKey;

    public EncryptedTcpConnection(String host, int port, Key encryptionKey) throws IOException {
        socket = new Socket(host, port);
        socket.setSoTimeout(RECEIVE_TIMEOUT);
        bufferedInputStream = new BufferedInputStream(socket.getInputStream());
        bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
        this.encryptionKey = encryptionKey;
    }

    public EncryptedTcpConnection(Socket socket, Key encryptionKey) throws IOException {
        this.socket = socket;
        socket.setSoTimeout(RECEIVE_TIMEOUT);
        bufferedInputStream = new BufferedInputStream(socket.getInputStream());
        bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
        this.encryptionKey = encryptionKey;
    }

    public void sendMessage(String message) throws IOException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        ChatPacket chatPacket = new ChatPacket(message, encryptionKey);
        bufferedOutputStream.write(chatPacket.getData());
        bufferedOutputStream.flush();
    }

    public String receiveMessage() throws IOException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] buffer = new byte[1024]; // TODO: CHANGE THIS BUFFER SIZE
        bufferedInputStream.read(buffer);
        return new ChatPacket(buffer).getMessage(encryptionKey);
    }

    public void close() {
        Utils.closeResource(socket);
    }
}
