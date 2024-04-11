package main.blog.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
public class apiResponseTest {

    @Test
    public void authResponseTest() {
        log.info("ressponse data ={}",ApiResponse.success(new ApiResponseMessage("1", "")));
    }
}
