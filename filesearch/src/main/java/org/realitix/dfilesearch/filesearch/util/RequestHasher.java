package org.realitix.dfilesearch.filesearch.util;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class RequestHasher {

    public static String hash(String request) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(request.getBytes());
        return new String(messageDigest.digest());
    }

}
