package edu.utdallas.cs5390.chat.framework.common.util;

import org.apache.commons.lang3.RandomStringUtils;

import javax.crypto.spec.SecretKeySpec;
import java.io.Closeable;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Adisor on 10/1/2016.
 */
public class Utils {

    public static final int CLIENT_UDP_TIMEOUT = 1000;
    public static final String MD5 = "MD5";
    public static final String SHA256 = "SHA-256";
    public static final String TIMEOUT_EXCEPTION = "TIMEOUT";

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

    public static String createHash(MessageDigest messageDigest, byte[] bytes) {
        messageDigest.update(bytes, 0, bytes.length);
        return new BigInteger(1, messageDigest.digest()).toString(16);
    }

    public static String createCipherKey(String rand, String key) throws NoSuchAlgorithmException {
        return createHash(MessageDigest.getInstance(Utils.SHA256), (rand + key).getBytes());
    }

    public static Key createEncryptionKey(String cipherKey) {
        return new SecretKeySpec(cipherKey.getBytes(), 0, 16, CIPHER_INSTANCE_TYPE);
    }

    public static String extractHeader(String message) {
        return message.substring(0, message.indexOf("(")).trim();
    }

    public static String extractValue(String message) {
        return message.substring(message.indexOf("(") + 1, message.lastIndexOf(")")).trim();
    }

    public static boolean hasHeader(String message) {
        return message.contains("(");
    }

    public static List<String> extractValues(String message) {
        List<String> values = new ArrayList<>();
        String valuesString = message.substring(message.indexOf("(") + 1, message.lastIndexOf(")"));
        Collections.addAll(values, valuesString.split(","));
        return values;
    }
}
