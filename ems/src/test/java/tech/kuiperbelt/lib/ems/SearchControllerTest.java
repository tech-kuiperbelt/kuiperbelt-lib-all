package tech.kuiperbelt.lib.ems;


import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tech.kuiperbelt.lib.ems.domain.Foo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SearchControllerTest extends IntegrationTest {



    @Autowired
    private MockMvc mvc;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    private TransactionTemplate transactionTemplate;

    @Autowired
    private MetaService metaService;

    private String newFieldName;

    @Before
    public void before() {
        transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.executeWithoutResult(ts -> {
            newFieldName = "field" + RandomStringUtils.randomAlphanumeric(5).toLowerCase();
            metaService.createFiled("Foo", FieldDescriptor.builder()
                    .name(newFieldName)
                    .dataType(DataType.STRING)
                    .indexed(false)
                    .build());
        });
    }

    @After
    public void after() {
        transactionTemplate.executeWithoutResult(ts -> {
            metaService.removeField("Foo", newFieldName);

        });
    }


    @SneakyThrows
    @Test
    public void searchOnExtensionFieldTest() {
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            sneakyRun(() -> {
                //create an entity instance with extension field
                String name1 = RandomStringUtils.randomAlphanumeric(10);
                String newFieldValue1 = RandomStringUtils.randomAlphanumeric(10);
                byte[] newFooBody = toJson(ImmutableMap.of(
                        Foo.Fields.name, name1,
                        Foo.Fields.status, Foo.Status.DISABLED,
                        newFieldName, newFieldValue1
                ));

                String fooLink = mvc.perform(post("/foos").contentType(MediaType.APPLICATION_JSON).content(newFooBody))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getHeader("Location");
                //verify search by filter which extension field is party search condition
                String filterStr = Foo.Fields.name + "==" + name1 + " and " + newFieldName + "==" + newFieldValue1;
                mvc.perform(get("/foos/search/findByFilter").param("filter", filterStr))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$._embedded.foos").isArray())
                        .andExpect(jsonPath("$._embedded.foos.length()").value(1))
                        .andExpect(jsonPath("$._embedded.foos[0]." + Foo.Fields.name).value(name1))
                        .andExpect(jsonPath("$._embedded.foos[0]." + newFieldName).value(newFieldValue1));

                mvc.perform(delete(fooLink))
                        .andExpect(status().isNoContent());
            });
        });
    }

    /**
     * We need control the transaction by ourselves because of "Auditing".
     */
    @Test
    @SneakyThrows
    public void auditOnExtensionField() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);

        //create an entity instance with extension field
        String name1 = RandomStringUtils.randomAlphanumeric(10);
        String newFieldValue1 = RandomStringUtils.randomAlphanumeric(10);
        byte[] newFooBody = toJson(ImmutableMap.of(
                Foo.Fields.name, name1,
                Foo.Fields.status, Foo.Status.DISABLED,
                newFieldName, newFieldValue1
        ));

        String fooLink = transactionTemplate.execute(ts ->
            sneakyRun(() -> mvc.perform(post("/foos").contentType(MediaType.APPLICATION_JSON).content(newFooBody))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getHeader("Location"))
        );
        //update the entity instance
        String name2 = RandomStringUtils.randomAlphanumeric(10);
        String newFieldValue2 = RandomStringUtils.randomAlphanumeric(10);
        byte[] patchFooBody = toJson(ImmutableMap.of(
                Foo.Fields.name, name2,
                newFieldName, newFieldValue2
        ));

        transactionTemplate.executeWithoutResult(transactionStatus -> {
            sneakyRun(() -> {
                mvc.perform(patch(fooLink).contentType(MediaType.APPLICATION_JSON).content(patchFooBody))
                        .andExpect(status().isNoContent());
            });
        });

        transactionTemplate.executeWithoutResult(transactionStatus -> {
            sneakyRun(() -> {
                mvc.perform(get(fooLink))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$." + Foo.Fields.name).value(name2))
                        .andExpect(jsonPath("$." + newFieldName).value(newFieldValue2));

                //verify audit
                mvc.perform(get(fooLink + "/audits/prior"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$." + Foo.Fields.name).value(name1))
                        .andExpect(jsonPath("$." + newFieldName).value(newFieldValue1));

                // clean up
                mvc.perform(delete(fooLink))
                        .andExpect(status().isNoContent());

            });
        });
    }
}
