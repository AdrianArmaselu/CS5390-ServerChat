package edu.utdallas.cs5390.chat;

import java.security.MessageDigest;
import java.math.*;

public interface serverauth {
    public static String getMD5Hash(String text) throws Exception {
        MessageDigest m = MessageDigest.getInstance("MD5");
        m.update(text.getBytes(), 0, text.length());
        return new BigInteger(1, m.digest()).toString(16);
    }

    public static String generateXRES(int rand, int clientKey) throws Exception {
        String text = Integer.toString(rand) + Integer.toString(clientKey);
        String hash = getMD5Hash(text);
        return hash;
    }

    public static int getRandomInt() {
        return (int) (Integer.MAX_VALUE * Math.random());
    }

    public static String getSHA256Hash(String text) throws Exception {
        MessageDigest m = MessageDigest.getInstance("SHA-256");
        m.update(text.getBytes(), 0, text.length());
        return new BigInteger(1, m.digest()).toString(16);
    }

    public static String generateCipherKey(int rand, int clientKey) throws Exception {
        String text = Integer.toString(rand) + Integer.toString(clientKey);
        String hash = getSHA256Hash(text);
        return hash;
    }
}
