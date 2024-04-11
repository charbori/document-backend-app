package main.blog.auth;


import lombok.extern.slf4j.Slf4j;
import main.blog.util.AES128Config;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@RunWith(SpringRunner.class)
public class AuthUtilTest {

    @Test
    public void testAuthUtil() throws NoSuchPaddingException, NoSuchAlgorithmException {
        AES128Config aes128Config = new AES128Config();
        String testString = "jaehyeok";
        String encryptString = aes128Config.encryptAes(testString);
        String decryptAes = aes128Config.decryptAes(encryptString);
        log.info(encryptString);
        log.info(decryptAes);
    }
}
