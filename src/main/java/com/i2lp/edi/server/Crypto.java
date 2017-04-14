package com.i2lp.edi.server;

import org.apache.commons.codec.digest.DigestUtils;

import java.security.SecureRandom;

/**
 * Created by amriksadhra on 22/03/2017.
 */
public class Crypto {
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        return bytes;
    }

    public static String calculateHash(String data, String salt) {
        return DigestUtils.sha512Hex(data + salt);
    }

    public static String bytetoString(byte[] input) {
        return org.apache.commons.codec.binary.Base64.encodeBase64String(input);
    }
}
