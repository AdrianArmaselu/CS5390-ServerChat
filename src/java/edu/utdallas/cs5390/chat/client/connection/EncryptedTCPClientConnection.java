package edu.utdallas.cs5390.chat.client.connection;

import edu.utdallas.cs5390.chat.util.Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Adisor on 10/1/2016.
 */

public class EncryptedTCPClientConnection extends AbstractClientConnection{
    private final Socket socket;
    private final BufferedReader bufferedReader;
    private final BufferedWriter bufferedWriter;
    private final Key key;

    public EncryptedTCPClientConnection(String host, int port, Key key) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        this.key = key;
        socket = new Socket(host, port);
        socket.setSoTimeout(RECEIVE_TIMEOUT);
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void sendMessage(String message) throws IOException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        bufferedWriter.write(Utils.cipherMessage(key, Cipher.ENCRYPT_MODE, message));
    }

    public String receiveMessage() throws IOException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        return Utils.cipherMessage(key, Cipher.DECRYPT_MODE, bufferedReader.readLine());
    }

    public void close() {
        Utils.closeResource(socket);
    }
}
