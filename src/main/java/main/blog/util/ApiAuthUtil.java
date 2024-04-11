package main.blog.util;

import main.blog.exception.AuthEncryptException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

public class ApiAuthUtil {
    private static final String[] permittedApiRequestKey = {"jaehyeok", "cjh93", "jaehyeok93"};

    private static AES128Config aes128Config = new AES128Config();

    public static String getAuthKeyPublished(String authKey) {
        return aes128Config.encryptAes(authKey);
    }
    public static boolean isApiAuthenticated(String authKey) {
        String decodedKey = aes128Config.decryptAes(authKey);
        if (Arrays.stream(permittedApiRequestKey).anyMatch(publishedKey -> publishedKey.equals(decodedKey))) {
            return true;
        } else {
            throw new AuthEncryptException();
        }
    }
}
