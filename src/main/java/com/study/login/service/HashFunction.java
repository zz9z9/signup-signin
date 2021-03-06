package com.study.login.service;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class HashFunction extends Encryption {

    public HashFunction() {
        super(HashAlgorithm.SHA256);
    }

    @Override
    public String getEncryptedPassword(String password, String salt) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(hashAlgorithm.getValue());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[] pw = password.getBytes();

        // key-stretching
        for (int i = 0; i < 10000; i++) {
            String pwSalt = byteToString(pw) + salt;
            md.update(pwSalt.getBytes());
            pw = md.digest();
        }

        return byteToString(pw);
    }
}
