package tech.kuiperbelt.lib.common.datarest;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tech.kuiperbelt.lib.common.IntegrationTest;
import tech.kuiperbelt.lib.common.domain.Foo;
import tech.kuiperbelt.lib.common.domain.FooRepository;
import tech.kuiperbelt.lib.common.util.JsonMapBuilder;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.kuiperbelt.lib.common.domain.Foo.Status.ENABLED;


public class DataRestControllerTest extends IntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private FooRepository fooRepository;

    private Foo savedFoo;

    @Before
    public void before() {
        savedFoo = fooRepository.save(Foo.builder()
                .aaa(RandomStringUtils.random(10))
                .ccc(RandomUtils.nextInt())
                .status(ENABLED)
                .build());
    }

    @Transactional
    @Test
    public void findAll() throws Exception {
        mvc.perform(get("/foos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$._embedded.foos").isArray())
                .andExpect(jsonPath("$._embedded.foos[0].id").value(String.valueOf(savedFoo.getId())));
    }

    @Transactional
    @Test
    public void findOne() throws Exception {
        mvc.perform(get("/foos/" + savedFoo.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aaa").value(savedFoo.getAaa()));
    }

    @Transactional
    @Test
    public void createOne() throws Exception {
        String randomStr = RandomStringUtils.random(10);
        JsonMapBuilder requestBody = new JsonMapBuilder()
                .add("aaa", randomStr)
                .add("bbb", RandomUtils.nextBoolean())
                .add("ccc", RandomUtils.nextInt())
                .add("status", Foo.Status.DISABLED);

        mvc.perform(post("/foos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody.toString()))
                .andExpect(status().isCreated());

        List<Foo> byaStr = fooRepository.findByAaa(randomStr);
        assertThat(byaStr, hasSize(1));
    }

    @Transactional
    @Test
    public void updateOne() throws Exception {
        String randomStr = RandomStringUtils.random(10);
        JsonMapBuilder requestBody = new JsonMapBuilder().add("aaa", randomStr);
        mvc.perform(put("/foos/" + savedFoo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody.toString()))
                .andExpect(status().isNoContent());
        List<Foo> byaStr = fooRepository.findByAaa(randomStr);
        assertThat(byaStr, hasSize(1));
        assertThat(byaStr.get(0).getStatus(), nullValue());
    }

    @Transactional
    @Test
    public void patchOne() throws Exception {
        Map<String, Object> content = new HashMap<>();
        String randomStr = RandomStringUtils.random(10);
        JsonMapBuilder requestBody = new JsonMapBuilder().add("aaa", randomStr);
        mvc.perform(patch("/foos/" + savedFoo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody.toString()))
                .andExpect(status().isNoContent());
        List<Foo> byaStr = fooRepository.findByAaa(randomStr);
        assertThat(byaStr, hasSize(1));
        assertThat(byaStr.get(0).getStatus(), equalTo(ENABLED));
    }

    @Transactional
    @Test
    public void deleteOne() throws Exception {
        mvc.perform(delete("/foos/" + savedFoo.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        Optional<Foo> byId = fooRepository.findById(savedFoo.getId());
        assertTrue(!byId.isPresent());
    }

    @Transactional
    @Test
    public void searchByNamedSearch() throws Exception {
        mvc.perform(get("/foos/search/findByAaa").queryParam("aaa", savedFoo.getAaa())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.foos").isArray())
                .andExpect(jsonPath("$._embedded.foos.length()").value(1))
                .andExpect(jsonPath("$._embedded.foos[0].id").value(String.valueOf(savedFoo.getId())));
    }

    @Transactional
    @Test
    public void searchByFilter() throws Exception {
        mvc.perform(get("/foos/search/findByFilter").queryParam("filter", "aaa==" + savedFoo.getAaa())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.foos").isArray())
                .andExpect(jsonPath("$._embedded.foos.length()").value(1))
                .andExpect(jsonPath("$._embedded.foos[0].id").value(String.valueOf(savedFoo.getId())));
    }

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    /**
     * audit testing need commit transactional
     * @throws Exception
     */
    @Test
    public void audits() throws Exception {
        TransactionTemplate template = new TransactionTemplate(platformTransactionManager);
        Foo existedFoo = template.execute(transactionStatus -> fooRepository.save(Foo.builder()
                .aaa(RandomStringUtils.random(10))
                .ccc(RandomUtils.nextInt())
                .status(ENABLED)
                .build()));
        try {
            mvc.perform(get("/foos/{foo-id}/audits", existedFoo.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.foos").isArray())
                    .andExpect(jsonPath("$._embedded.foos.length()").value(1))
                    .andExpect(jsonPath("$._embedded.foos[0].id").value(String.valueOf(existedFoo.getId())));

        } finally {
            template.executeWithoutResult(transactionStatus -> fooRepository.deleteAll());
        }
    }
}