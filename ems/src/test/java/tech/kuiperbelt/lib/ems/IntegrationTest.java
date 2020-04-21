package tech.kuiperbelt.lib.ems;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.concurrent.Callable;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment=RANDOM_PORT)
@ActiveProfiles({"test"})
@AutoConfigureMockMvc
public abstract class IntegrationTest {
    @Autowired
    private ObjectMapper objectMapper;

    public static void  sneakyRun(SearchControllerTest.ExceptionRunnable exceptionRunnable) {
        try {
            exceptionRunnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <V> V sneakyRun(Callable<V> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @SneakyThrows
    byte[] toJson(Map<String, Object> map) {
        return objectMapper.writeValueAsBytes(map);
    }

    @FunctionalInterface
    public interface ExceptionRunnable {
        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see     java.lang.Thread#run()
         */
        public abstract void run() throws Exception;
    }
}
