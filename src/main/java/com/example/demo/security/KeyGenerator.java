package com.example.demo.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

//import javax.crypto.SecretKey;
//
//public class KeyGenerator  {
//    public static void main(String[] args) {
//        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS384); // Generate a secure key
//        String base64EncodedKey = Encoders.BASE64.encode(key.getEncoded());
//        System.out.println("Base64 Encoded Key: " + base64EncodedKey);
//    }
//}
