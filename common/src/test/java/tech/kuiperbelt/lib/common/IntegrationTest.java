package tech.kuiperbelt.lib.common;


import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment=RANDOM_PORT)
@ActiveProfiles({"test"})
@AutoConfigureMockMvc
public abstract class IntegrationTest {

}
