package edu.utdallas.cs5390.chat.framework.common.util;

import org.apache.commons.lang3.RandomStringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.Closeable;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Adisor on 10/1/2016.
 */
public class Utils {

    public static final String MD5 = "MD5";
    public static final String SHA256 = "SHA-256";

    public static String CIPHER_INSTANCE_TYPE = "AES";


    public static int seconds(int seconds) {
        return seconds * 1000;
    }

    public static int megabytes(int megabytes) {
        return megabytes * 1024 * 1024;
    }

    public static int kilobytes(int kilobytes) {
        return kilobytes * 1024;
    }

    public static void closeResource(Closeable closeable) {
        if (closeable != null)
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static String UUID() {
        return String.valueOf(java.util.UUID.randomUUID());
    }

    public static String randomString(int size) {
        return RandomStringUtils.random(size, true, true);
    }

    public static String createHash(MessageDigest messageDigest, String code) {
        messageDigest.update(code.getBytes(), 0, code.length());
        return new BigInteger(1, messageDigest.digest()).toString(16);
    }

    public static String cipherMessage(Key key, int cipherMode, String message) throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher decipher = Cipher.getInstance(CIPHER_INSTANCE_TYPE);
        decipher.init(cipherMode, key);
        byte[] decryptedResponseBytes = decipher.doFinal(message.getBytes());
        return new String(decryptedResponseBytes);
    }

    public static String encryptMessage(Key key, String message) throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        return cipherMessage(key, Cipher.ENCRYPT_MODE, message);
    }

    public static String decryptMessage(Key key, String message) throws IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException {
        return cipherMessage(key, Cipher.DECRYPT_MODE, message);
    }

    public static String createCipherKey(String rand, String key) throws NoSuchAlgorithmException {
        return createHash(MessageDigest.getInstance(Utils.SHA256), rand + key);
    }

    public static Key createEncryptionKey(String cipherKey) {
        return new SecretKeySpec(cipherKey.getBytes(), 0, 16, "AES");
    }

    public static Key createEncryptionKey(String rand, String key) throws NoSuchAlgorithmException {
        String cipherKey = createCipherKey(rand, key);
        return new SecretKeySpec(cipherKey.getBytes(), 0, 16, "AES");
    }

    public static String extractProtocolHeader(String message) {
        return message.substring(0, message.indexOf("("));
    }

    public static String extractMessage(DatagramPacket datagramPacket) {
        byte[] messageBytes = datagramPacket.getData();
        return new String(messageBytes);
    }

    public static String extractValue(String message) {
        return message.substring(message.indexOf("(") + 1, message.lastIndexOf(")"));
    }
}
