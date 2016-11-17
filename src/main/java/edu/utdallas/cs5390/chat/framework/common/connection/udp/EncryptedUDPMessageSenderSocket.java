package edu.utdallas.cs5390.chat.framework.common.connection.udp;

import edu.utdallas.cs5390.chat.framework.common.util.Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * Created by adisor on 11/1/2016.
 */
public class EncryptedUDPMessageSenderSocket extends UDPMessageSenderSocket {
    private Key encryptionKey;

    public EncryptedUDPMessageSenderSocket() throws SocketException {
        super();
    }

    public EncryptedUDPMessageSenderSocket(Key encryptionKey) throws SocketException {
        this();
        this.encryptionKey = encryptionKey;
    }

    public void setEncryptionKey(Key encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public void sendMessage(String message, InetAddress receiverAddress, int receiverPort) throws IOException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException {
        byte[] encryptedMessageBytes = Utils.encryptMessage(encryptionKey, message).getBytes();
        datagramSocket.send(new DatagramPacket(encryptedMessageBytes, message.length(), receiverAddress, receiverPort));
    }
}
