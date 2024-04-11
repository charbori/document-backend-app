package main.blog.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import main.blog.exception.AuthEncryptException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public class AES128Config {
    private static final String ALGORITHM = "AES";

    @Value("${aes.secret-key}")
    private String secretKey;
    KeyGenerator keyGen = null;

    public AES128Config() {
        secretKey = "QsbkGrY4rVYlVy6t/z25Bw==";
        try {
            keyGen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyGen.init(128); // for AES-128. Use 192 for AES-192, or 256 for AES-256.
    }

    // AES 암호화
    public String encryptAes(String plaintext) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedByteValue = cipher.doFinal(plaintext.getBytes("utf-8"));
            return Base64.getEncoder().encodeToString(encryptedByteValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // AES 복호화
    public String decryptAes(String plaintext) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryptedValue64 = Base64.getDecoder().decode(plaintext);
            byte[] decryptedByteValue = cipher.doFinal(decryptedValue64);
            return new String(decryptedByteValue, "utf-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
