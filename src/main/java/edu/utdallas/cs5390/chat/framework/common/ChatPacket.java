package edu.utdallas.cs5390.chat.framework.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static edu.utdallas.cs5390.chat.framework.common.util.Utils.CIPHER_INSTANCE_TYPE;

/**
 * Created by adisor on 11/21/2016.
 */
public class ChatPacket {
    private final Logger logger = LoggerFactory.getLogger(ChatPacket.class);

    private static final int sizeBytesSize = 4;
    private static final int encryptionFlagBytesSize = 1;

    private byte[] message;
    private boolean isEncrypted;

    public ChatPacket(byte[] buffer) {
        logger.debug("Creating a new chat packet from buffer with bytes " + Arrays.toString(buffer));

        int messageSize = getMessageSize(buffer);
        this.message = new byte[messageSize];

        copyMessageBytes(buffer, message.length);
        logger.debug("Copied message bytes from the buffer to packet");

        this.isEncrypted = buffer[0] == 1;
        logger.debug(String.format("Created chat packet with size %d (messageBytes:%s, isEncrypted:%b)", messageSize, Arrays.toString(message), this.isEncrypted));
    }

    public ChatPacket(String message) throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException {
        this(message, null);
    }

    public ChatPacket(String message, Key encryptionKey) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        setMessage(message, encryptionKey);
        logger.debug("Creating a new chat packet from message " + message + (encryptionKey == null? " with no encryption key": " with encryption key"));
    }

    private int getMessageSize(byte[] buffer){
        byte[] sizeBytes = new byte[sizeBytesSize];
        copySizeBytes(buffer, sizeBytes);
        return new BigInteger(sizeBytes).intValue();
    }

    private void copySizeBytes(byte[] buffer, byte[] sizeBytes){
        System.arraycopy(buffer, encryptionFlagBytesSize, sizeBytes, 0, sizeBytesSize);
    }

    private void copyMessageBytes(byte[] buffer, int length){
        System.arraycopy(buffer, sizeBytesSize + encryptionFlagBytesSize, message, 0, length);
    }

    public byte[] getData() {
        int totalSize = message.length + sizeBytesSize + encryptionFlagBytesSize;
        byte[] data = new byte[totalSize];
        data[0] = (byte) (isEncrypted ? 1 : 0);
        data[1] = (byte) (message.length >> 24);
        data[2] = (byte) ((message.length >> 16) & 0xFF);
        data[3] = (byte) ((message.length >> 8) & 0xFF);
        data[4] = (byte) ((message.length) & 0xFF);
        System.arraycopy(message, 0, data, sizeBytesSize + encryptionFlagBytesSize, message.length);
        logger.debug(String.format("Wrapped message bytes: %s of size %d with encryption=%b data into packet: %s",
                Arrays.toString(message), message.length, isEncrypted, Arrays.toString(data)));
        return data;
    }

    public String getMessage(Key encryptionKey) throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException {
        if (isEncrypted) {
            if(encryptionKey == null) throw new IllegalArgumentException("Reading the message requires decryption, but now encryption key is provided");
            return new String(decryptMessage(message, encryptionKey));
        } else
            return new String(message);
    }

    public void setMessage(String message, Key encryptionKey) throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException {
        if (encryptionKey != null) {
            this.message = encryptMessage(message.getBytes(), encryptionKey);
            this.isEncrypted = true;
        } else {
            this.message = message.getBytes();
            this.isEncrypted = false;
        }
    }

    private static byte[] cipherMessage(Key key, int cipherMode, byte[] message) throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE_TYPE);
        cipher.init(cipherMode, key);
        return cipher.doFinal(message);
    }

    private static byte[] encryptMessage(byte[] message, Key key) throws IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException {
        return cipherMessage(key, Cipher.ENCRYPT_MODE, message);
    }

    private static byte[] decryptMessage(byte[] message, Key key) throws IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException {
        return cipherMessage(key, Cipher.DECRYPT_MODE, message);
    }


    public boolean isEncrypted() {
        return isEncrypted;
    }
}
