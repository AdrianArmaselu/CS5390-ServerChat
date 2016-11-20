package edu.utdallas.cs5390.chat.framework.server.service;

import edu.utdallas.cs5390.chat.framework.common.util.Utils;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Adisor on 10/1/2016.
 */
public class ClientProfile {
    public String username;
    public String password;
    public String rand;
    public boolean isRegistered;
    public String cipherKey;
    public Key encryptionKey;

    public ClientProfile() {
    }

    public void createEncryptionKeys() throws NoSuchAlgorithmException {
        cipherKey = Utils.createCipherKey(rand, password);
        encryptionKey = Utils.createEncryptionKey(cipherKey);
    }
}
