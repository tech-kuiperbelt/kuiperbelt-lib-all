package tech.kuiperbelt.lib.ems;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.hal.HalMediaTypeConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Meta Service 对外暴露的API
 */
@RestController
public class MetaController {

    public static final String PATH_PREFIX = "/meta";
    public static final String LOCATION = "Location";
    public static final String SELF = "self";

    @Autowired
    private MetaService metaService;

    @GetMapping(PATH_PREFIX + "/entities")
    public CollectionModel<EntityModel<EntityDescriptor>> allEntities() {
        List<EntityModel<EntityDescriptor>> models = metaService.findAllEntities().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        Link link = linkTo(methodOn(MetaController.class).allEntities()).withSelfRel();
        return new CollectionModel<>(models, link);
    }

    @GetMapping(PATH_PREFIX + "/entities/{entity-name}")
    public EntityModel<EntityDescriptor> getEntity(@PathVariable("entity-name") String entityName) {
        return toModel(metaService.getEntity(entityName));
    }

    private EntityModel<EntityDescriptor> toModel(EntityDescriptor entityDescriptor) {
        Link link = linkTo(methodOn(MetaController.class).getEntity(entityDescriptor.getName())).withSelfRel();
        return new EntityModel(entityDescriptor,link);
    }

    /*************************** field api *****************************/

    @GetMapping(PATH_PREFIX + "/entities/{entity-name}/fields")
    public CollectionModel<EntityModel<FieldDescriptor>> getFields(@PathVariable("entity-name") String entityName) {
        List<EntityModel<FieldDescriptor>> collect = metaService.findFields(entityName).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        Link link = linkTo(methodOn(MetaController.class).getFields(entityName)).withSelfRel();

        return new CollectionModel<>(collect, link);
    }

    @GetMapping(PATH_PREFIX + "/entities/{entity-name}/fields/{field-name}")
    public EntityModel<FieldDescriptor> getField(@PathVariable("entity-name") String entityName, @PathVariable("field-name") String fieldName) {
        return toModel(metaService.getField(entityName, fieldName));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(PATH_PREFIX + "/entities/{entity-name}/fields")
    public ResponseEntity<EntityModel<FieldDescriptor>> createField(@PathVariable("entity-name") String entityName,
                            @RequestBody FieldDescriptor fieldDescriptor) {
        return buildCreatedResponseEntity(toModel(metaService.createFiled(entityName, fieldDescriptor)));
    }


    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(PATH_PREFIX + "/entities/{entity-name}/fields/{field-name}")
    public void deleteField(@PathVariable("entity-name") String entityName, @PathVariable("field-name") String fieldName) {
        metaService.removeField(entityName, fieldName);
    }

    private EntityModel<FieldDescriptor> toModel(FieldDescriptor fieldDescriptor) {
        Link link = linkTo(methodOn(MetaController.class).getField(fieldDescriptor.getEntity(), fieldDescriptor.getName())).withSelfRel();
        return new EntityModel(fieldDescriptor,link);
    }

    /*************************** event api *****************************/

    @GetMapping(PATH_PREFIX + "/entities/{entity-name}/event-handlers")
    public CollectionModel<EntityModel<EventHandler>> getEventHandlers(@PathVariable("entity-name") String entityName) {
        List<EntityModel<EventHandler>> models = metaService.findEventHandlers(entityName).stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        Link link = linkTo(methodOn(MetaController.class).getEventHandlers(entityName)).withSelfRel();
        return new CollectionModel(models, link);
    }

    @GetMapping(PATH_PREFIX + "/entities/{entity-name}/event-handlers/{event-handler-name}")
    public EntityModel<EventHandler> getEventHandler(@PathVariable("entity-name") String entityName,
                @PathVariable("event-handler-name") String eventHandlerName) {
        return toModel(metaService.getEventHandler(entityName, eventHandlerName));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(PATH_PREFIX + "/entities/{entity-name}/event-handlers")
    public ResponseEntity<EntityModel<EventHandler>> attachEventHandler(@PathVariable("entity-name") String entityName, @RequestBody EventHandler eventHandler) {
        return buildCreatedResponseEntity(toModel(metaService.attachEventHandler(entityName, eventHandler)));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(PATH_PREFIX + "/entities/{entity-name}/event-handlers/{handler-name}")
    public void detachEventHandler(@PathVariable("entity-name") String entityName,
                                   @PathVariable("handler-name") String handlerName) {
        metaService.detachEventHandler(entityName, handlerName);
    }


    private EntityModel<EventHandler> toModel(EventHandler eventHandler) {
        Link link = linkTo(methodOn(MetaController.class).getEventHandler(eventHandler.getEntity(), eventHandler.getName())).withSelfRel();
        return new EntityModel(eventHandler,link);
    }

    /*************************** enum api *****************************/
    @GetMapping(PATH_PREFIX + "/enums")
    public CollectionModel<EntityModel<EnumDescriptor>> getEnums() {
        List<EntityModel<EnumDescriptor>> models = metaService.getAllEnums().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        Link link = linkTo(methodOn(MetaController.class).getEnums()).withSelfRel();
        return new CollectionModel<>(models, link);
    }

    @GetMapping(PATH_PREFIX + "/enums/{enum-name}")
    public EntityModel<EnumDescriptor> getEnum(@PathVariable("enum-name") String enumName) {
        return toModel(metaService.getEnumValue(enumName));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(PATH_PREFIX + "/enums")
    public ResponseEntity<EntityModel<EnumDescriptor>> addEnum(@RequestBody EnumDescriptor enumDescriptor) {
        return buildCreatedResponseEntity(toModel(metaService.createEnum(enumDescriptor)));
    }

    @PutMapping(PATH_PREFIX + "/enums/{enum-name}")
    public EntityModel<EnumDescriptor> updateEnum(@PathVariable("enum-name") String enumName, @RequestBody EnumDescriptor enumDescriptor) {
        return toModel(metaService.updateEnum(enumName, enumDescriptor));
    }

    private EntityModel<EnumDescriptor> toModel(EnumDescriptor enumDescriptor) {
        Link link = linkTo(methodOn(MetaController.class).getEnum(enumDescriptor.getName())).withSelfRel();
        return new EntityModel(enumDescriptor,link);
    }

    /*************************** dataType api *****************************/
    @GetMapping(value = PATH_PREFIX + "/data-types", produces = MediaType.APPLICATION_JSON_VALUE)
    public byte[] getDataTypes() throws JsonProcessingException {
        ObjectMapper objectMapper = getObjectMapperForDataType();

        List<EntityModel<DataType>> models = Stream.of(DataType.values())
                .map(this::toModel)
                .collect(Collectors.toList());
        Link link = linkTo(MetaController.class).slash(PATH_PREFIX + "/data-types/").withSelfRel();
        CollectionModel<EntityModel<DataType>> result = new CollectionModel<>(models, link);

        return objectMapper.writeValueAsBytes(result);
    }

    @GetMapping(value = PATH_PREFIX + "/data-types/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public byte[] getDataType(@PathVariable("name") String name) throws JsonProcessingException {
        ObjectMapper objectMapper = getObjectMapperForDataType();
        DataType dataType = DataType.valueOf(name);
        return objectMapper.writeValueAsBytes(toModel(dataType));
    }

    private EntityModel<DataType> toModel(DataType dataType) {
        Link link = linkTo(MetaController.class).slash(PATH_PREFIX + "/data-types/" + dataType.name()).withSelfRel();
        return new EntityModel(dataType,link);
    }

    private ObjectMapper getObjectMapperForDataType() {
        return dataTypeObjectMapper;
    }

    private ObjectMapper dataTypeObjectMapper;

    public MetaController(HalMediaTypeConfiguration configuration) {
        dataTypeObjectMapper = configuration.configureObjectMapper(new ObjectMapper());
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataType.class, new DataTypeSerializer());
        dataTypeObjectMapper.registerModule(module);
    }


    private <T extends EntityModel<?>> ResponseEntity<T> buildCreatedResponseEntity(T result) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(LOCATION, result.getLink(SELF).map(Link::getHref).orElse(""))
                .body(result);
    }
}
