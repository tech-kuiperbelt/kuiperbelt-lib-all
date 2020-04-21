package tech.kuiperbelt.lib.ems;

import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.text.StringSubstitutor;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tech.kuiperbelt.lib.common.jpa.BaseEntity;
import tech.kuiperbelt.lib.ems.domain.Foo;

import javax.transaction.Transactional;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MetaControllerTest extends IntegrationTest {


    @Autowired
    private MockMvc mvc;

    @SneakyThrows
    @Test
    void allEntities() {
        mvc.perform(get("/meta/entities")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.entityDescriptors").isArray())
                .andExpect(jsonPath("$._embedded.entityDescriptors.length()").value(1))
                .andExpect(jsonPath("$._embedded.entityDescriptors[0].name").value("Foo"));
    }

    @SneakyThrows
    @Test
    void getEntity() {
        mvc.perform(get("/meta/entities/Foo")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Foo"));
    }

    @SneakyThrows
    @Test
    void getFields() {
        mvc.perform(get("/meta/entities/Foo/fields")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.fieldDescriptors").isArray())
                .andExpect(jsonPath("$._embedded.fieldDescriptors.length()").value(8))
                .andExpect(jsonPath("$._embedded.fieldDescriptors[*].name").value(hasItems(
                        BaseEntity.Fields.createdBy, BaseEntity.Fields.id, Foo.Fields.name, Foo.Fields.status)));
    }

    @SneakyThrows
    @Test
    void getField() {
        mvc.perform(get("/meta/entities/Foo/fields/{field-name}", Foo.Fields.name)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataType").value(DataType.STRING.name()))
                .andExpect(jsonPath("$.domainField").value(Foo.Fields.name));
    }

    @Transactional
    @SneakyThrows
    @Test
    void createAndDeleteField() {
        //create field
        final String newFieldName = createExtensionField("Foo", DataType.STRING, false);

        //fetch new field from Entity Foo
        mvc.perform(get("/meta/entities/Foo/fields/{field-name}", newFieldName)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataType").value(DataType.STRING.name()))
                .andExpect(jsonPath("$.extension").value(true))
                .andExpect(jsonPath("$.indexed").value(false))
                .andExpect(jsonPath("$.domainField").value(startsWith("str")));

        // delete new field
        deleteExtensionField("Foo", newFieldName);
        //verify the field deleted
        mvc.perform(get("/meta/entities/Foo/fields/{field-name}", newFieldName)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @SneakyThrows
    private void deleteExtensionField(String entityName, String fieldName) {
        // delete new field
        mvc.perform(delete("/meta/entities/{entity-name}/fields/{field-name}", entityName, fieldName)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

    }

    @SneakyThrows
    private String createExtensionField(String entityName, DataType dataType, boolean indexed) {
        final String newFieldName = "field" + RandomStringUtils.randomAlphanumeric(5).toLowerCase();

        //create new field on Entity Foo
        byte[] requestBody = toJson(ImmutableMap.of(
                "name", newFieldName,
                "dataType", dataType.name(),
                "indexed", indexed
        ));

        mvc.perform(post("/meta/entities/{entity-name}/fields", entityName)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        return newFieldName;
    }

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Ignore
    @SneakyThrows
    @Test
    public void curdOnExtensionFields() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);

        //create field
        final String strField1 = transactionTemplate.execute(ts -> createExtensionField("Foo", DataType.STRING, false));
        final String intField1 = transactionTemplate.execute(ts -> createExtensionField("Foo", DataType.INT, false));
        try {
            String name = RandomStringUtils.randomAlphabetic(10);
            String strField1Value = RandomStringUtils.randomAlphabetic(10);
            int intField1Value = RandomUtils.nextInt();

            // create entity with extension fields
            byte[] createRequestBody = toJson(ImmutableMap.of(
                    Foo.Fields.name, name,
                    Foo.Fields.status, Foo.Status.DISABLED,
                    strField1, strField1Value,
                    intField1, intField1Value
            ));
            MvcResult mvcResult = mvc.perform(post("/foos").content(createRequestBody).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andReturn();
            String link = mvcResult.getResponse().getHeader("Location");

            //verify create
            mvc.perform(get(link))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$." + Foo.Fields.name).value(name))
                    .andExpect(jsonPath("$." + Foo.Fields.status).value(Foo.Status.DISABLED.name()))
                    .andExpect(jsonPath("$." + strField1).value(strField1Value))
                    .andExpect(jsonPath("$." + intField1).value(intField1Value));

            // patch operation
            String newStrField1Value = RandomStringUtils.randomAlphabetic(10);
            byte[] patchRequestBody = toJson(ImmutableMap.of(
                    "status", Foo.Status.ENABLED,
                    strField1, newStrField1Value
            ));
            mvc.perform(patch(link).content(patchRequestBody).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            //verify patch
            mvc.perform(get(link))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$." + Foo.Fields.name).value(name))
                    .andExpect(jsonPath("$." + Foo.Fields.status).value(Foo.Status.ENABLED.name()))
                    .andExpect(jsonPath("$." + strField1).value(newStrField1Value))
                    .andExpect(jsonPath("$." + intField1).value(intField1Value));

            // update operation
            Integer newIntField1Value = RandomUtils.nextInt();
            byte[] updateRequestBody = toJson(ImmutableMap.of(
                    Foo.Fields.name, name,
                    intField1, newIntField1Value
            ));
            mvc.perform(put(link).content(updateRequestBody).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            //verify update
            mvc.perform(get(link))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$." + Foo.Fields.name).value(name))
                    .andExpect(jsonPath("$." + Foo.Fields.status).doesNotExist())
                    .andExpect(jsonPath("$." + strField1).doesNotExist())
                    .andExpect(jsonPath("$." + intField1).value(newIntField1Value));

            // delete operation
            mvc.perform(delete(link))
                    .andExpect(status().isNoContent());

            // verify delete
            mvc.perform(get(link))
                    .andExpect(status().isNotFound());
        } finally {
            transactionTemplate.executeWithoutResult(ts -> deleteExtensionField("Foo", strField1));
            transactionTemplate.executeWithoutResult(ts -> deleteExtensionField("Foo", intField1));
        }

    }

    @Transactional
    @SneakyThrows
    @Test
    void testEventHandlers() {
        //prepared an extension filed
        final String strField1 = createExtensionField("Foo", DataType.STRING, false);


        //fetch eventHandler
        String eventHandlerEntryPoint = "/meta/entities/Foo/event-handlers";
        mvc.perform(get(eventHandlerEntryPoint).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());

        //attach eventHandler
        StringSubstitutor params = new StringSubstitutor(ImmutableMap.of(
                "ext-field-name", strField1
        ));
        String groovyScriptTemplate = "def handler(event) { " +
                " event.current.${ext-field-name} = event.current.${ext-field-name} + event.current.name " +
                "}";
        String groovyScript = params.replace(groovyScriptTemplate);
        String eventName = RandomStringUtils.randomAlphabetic(5);
        byte[] attachEventRequestBody = toJson(ImmutableMap.of(
                EventHandler.Fields.name, eventName,
                EventHandler.Fields.type, EntityEvent.Type.BEFORE_UPDATE.name(),
                EventHandler.Fields.ranking, 0,
                EventHandler.Fields.script, groovyScript
        ));
        String eventHandlerLink = mvc.perform(post(eventHandlerEntryPoint).content(attachEventRequestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        //verify attached EventHandler by fetch
        mvc.perform(get(eventHandlerLink).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + EventHandler.Fields.name).value(eventName));

        //prepared an entity instance with value of extension field
        String name = RandomStringUtils.randomAlphabetic(10);
        String strField1Value = RandomStringUtils.randomAlphabetic(10);

        byte[] createRequestBody = toJson(ImmutableMap.of(
                Foo.Fields.name, name,
                Foo.Fields.status, Foo.Status.DISABLED,
                strField1, strField1Value
        ));
        MvcResult mvcResult = mvc.perform(post("/foos").content(createRequestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        String entityLink = mvcResult.getResponse().getHeader("Location");

        //update entity and trigger event handler
        byte[] patchRequestBody = toJson(ImmutableMap.of(
                Foo.Fields.status, Foo.Status.ENABLED
        ));
        mvc.perform(patch(entityLink).content(patchRequestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mvc.perform(get(entityLink))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + strField1).value(strField1Value + name));

        //detach event handler
        mvc.perform(delete(eventHandlerLink))
                .andExpect(status().isNoContent());

        //verify detached EventHandler by fetch
        mvc.perform(get(eventHandlerLink))
                .andExpect(status().isNotFound());

    }



    @Transactional
    @SneakyThrows
    @Test
    void enumTest() {
        //fetch enums for oob
        mvc.perform(get("/meta/enums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.enumDescriptors").isArray())
                .andExpect(jsonPath("$._embedded.enumDescriptors.length()").value(1))
                .andExpect(jsonPath("$._embedded.enumDescriptors[0].name").value(Foo.Status.class.getName()));

        //create enum
        byte[] createEnumRequestBody = toJson(ImmutableMap.of(
                "name", "STATUS",
                "values", new Object[] {
                        ImmutableMap.of(
                                "label", "Running",
                                "value", "RUNNING",
                                "sequence", 100
                        ),
                        ImmutableMap.of(
                                "label", "Endding",
                                "value", "ENDDING",
                                "sequence", 200
                        ),
                        ImmutableMap.of(
                                "label", "Pending",
                                "value", "PENDING",
                                "sequence", 300
                        ),
                }
        ));
        String enumLink = mvc.perform(post("/meta/enums").contentType(MediaType.APPLICATION_JSON).content(createEnumRequestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        //verify by fetching enum which created
        mvc.perform(get(enumLink))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("STATUS"))
                .andExpect(jsonPath("$.extension").value(true))
                .andExpect(jsonPath("$.disable").value(false))
                .andExpect(jsonPath("$.values.length()").value(3))
                .andExpect(jsonPath("$.values[0].disable").value(false))
                .andExpect(jsonPath("$.values[0].value").value("RUNNING"));
        //update enum and disable enum value
        byte[] updateEnumRequestBody = toJson(ImmutableMap.of(
                "name", "STATUS",
                "extension", true,
                "disable", false,
                "values", new Object[] {
                        ImmutableMap.of(
                                "label", "Running",
                                "value", "RUNNING",
                                "sequence", 500
                        ),
                        ImmutableMap.of(
                                "label", "Endding",
                                "value", "ENDDING",
                                "sequence", 200
                        ),
                        ImmutableMap.of(
                                "label", "Pending",
                                "value", "PENDING",
                                "sequence", 300
                        ),
                }
        ));
        mvc.perform(put(enumLink).content(updateEnumRequestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // verify the result of update
        mvc.perform(get(enumLink))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("STATUS"))
                .andExpect(jsonPath("$.extension").value(true))
                .andExpect(jsonPath("$.values[0].value").value("ENDDING"));

        //add field which dataType is customized enum
        final String newFieldName = "field" + RandomStringUtils.randomAlphanumeric(5).toLowerCase();

        //create new field on Entity Foo
        byte[] createFieldRequestBody = toJson(ImmutableMap.of(
                "name", newFieldName,
                "dataType", DataType.ENUM.name(),
                    FieldDescriptor.Fields.enumRef, "STATUS",
                "indexed", false
        ));

        mvc.perform(post("/meta/entities/{entity-name}/fields", "Foo")
                .content(createFieldRequestBody)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        //create entity instance with new field
        byte[] newFooRequestBody = toJson(ImmutableMap.of(
            Foo.Fields.name, "test",
                newFieldName, "ENDDING"
        ));
        String entityLink = mvc.perform(post("/foos").contentType(MediaType.APPLICATION_JSON).content(newFooRequestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");
        //verify new field value
        mvc.perform(get(entityLink))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + newFieldName).value("ENDDING"));

        byte[] patchFooRequestBody = toJson(ImmutableMap.of(
                newFieldName, "ENDDING111"
        ));

        //create entity instance which new field value was not valid
        mvc.perform(patch(entityLink).content(patchFooRequestBody).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

    }


    @Transactional
    @SneakyThrows
    @Test
    void testDataType() {
        mvc.perform(get("/meta/data-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.dataTypes").isArray())
                .andExpect(jsonPath("$._embedded.dataTypes.length()").value(8));

        mvc.perform(get("/meta/data-types/STRING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indexable").value(true))
                .andExpect(jsonPath("$.extensible").value(true));

    }
}