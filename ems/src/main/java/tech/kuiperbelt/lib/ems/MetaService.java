package tech.kuiperbelt.lib.ems;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Meta Service
 */
@Slf4j
@Service
@Transactional
@RepositoryEventHandler
public class MetaService {

    public static final String TECH_KUIPERBELT_EMS_META_CACHE_MAP = "tech.kuiperbelt.ems.meta.cache.map";
    private static MetaService instance;

    public static Optional<MetaService> getInstance() {
        return Optional.ofNullable(instance);
    }

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ResourceMappings mappings;

    @Autowired
    private MetaDelegateRepository metaDelegateRepository;

    @Transactional
    @PostConstruct
    public void init() {
        entityManager
                .getMetamodel()
                .getEntities()
                .stream()
                .filter(entityType ->  mappings.getMetadataFor(entityType.getJavaType()).isExported())
                .forEach(this::syncWithEntityDescriptor);
        instance = this;
    }

    public List<EntityDescriptor> findAllEntities() {
        return metaDelegateRepository.findAllEntityDescriptors();
    }

    public EntityDescriptor getEntity(String entityName) {
        return metaDelegateRepository.getEntityDescriptorByName(entityName);
    }

    public EntityDescriptor getEntity(Class<? extends EmsEntity> aClass) {
        EntityType<? extends EmsEntity> entityType = entityManager.getMetamodel().entity(aClass);
        return this.metaDelegateRepository.getEntityDescriptorByName(entityType.getName());
    }

    private <X> void syncWithEntityDescriptor(EntityType<X> entityType) {
        String entityName = entityType.getName();

        //save if not existed
        Optional<EntityDescriptor> byName = metaDelegateRepository.findEntityDescriptorByName(entityName);
        ResourceMetadata resourceMetadata = mappings.getMetadataFor(entityType.getJavaType());

        EntityDescriptor entityDescriptor;
        if(!byName.isPresent() ) {
            entityDescriptor = metaDelegateRepository.save(EntityDescriptor.builder()
                    .name(entityType.getName())
                    .domainClass(entityType.getJavaType().getName())
                    .rel(resourceMetadata.getRel().value())
                    .build(), true);
        } else {
            entityDescriptor = byName.get();
        }

        //Sync Fields
        entityType.getAttributes().stream()
                .forEach(attribute -> syncWithFieldDescriptor(entityDescriptor, attribute));
    }

    private <X> void syncWithFieldDescriptor(EntityDescriptor entityDescriptor, Attribute<? super X, ?> attribute) {
        String attributeName = attribute.getName();
        getDataType(attribute)
                .ifPresent(dataType -> {
                    Optional<FieldDescriptor> byName = metaDelegateRepository.findFieldDescriptorByEntityAndName(entityDescriptor.getName(), attributeName);
                    if(!byName.isPresent()) {
                        metaDelegateRepository.save(enhanceWithFieldMeta(FieldDescriptor.builder()
                                .name(attributeName)
                                .label(attributeName)
                                .dataType(dataType)
                                .entity(entityDescriptor.getName())
                                .domainField(attribute.getName())
                                .enumRef(getEnumRef(attribute))
                                .build(), entityDescriptor.getDomainClass()), true);
                    }
                });
    }

    private <X> String getEnumRef(Attribute<? super X, ?> attribute) {
        String enumRef;
        if(attribute.getJavaType().isEnum()) {
            EnumDescriptor enumDescriptor = metaDelegateRepository.createEnum(EnumDescriptor.buildFromJavaEnum((Class<? extends Enum<?>>) attribute.getJavaType()), true);
            enumRef = enumDescriptor.getName();
        } else {
            enumRef = null;
        }
        return enumRef;
    }

    private FieldDescriptor enhanceWithFieldMeta(FieldDescriptor fieldDescriptor, String className) {
        Field field = getFieldRecursive(getClass(className), fieldDescriptor.getName());
        if(field != null) {
            FieldMeta annotation = AnnotationUtils.findAnnotation(field, FieldMeta.class);
            if(annotation != null) {
                if(annotation.label() != null) {
                    fieldDescriptor.setLabel(annotation.label());
                }
                fieldDescriptor.setIndexed(annotation.indexed());
                if(annotation.dataType() != null) {
                    fieldDescriptor.setDataType(annotation.dataType());
                }

            }
            return fieldDescriptor;
        } else {
            throw new IllegalStateException("can not find java field with fieldDescriptor: " + fieldDescriptor);
        }
    }

    @SneakyThrows
    private Class<?> getClass(String clazzName) {
        return Class.forName(clazzName);
    }

    private Field getFieldRecursive(Class<?> clazz, String name) {
        for(Field f: clazz.getDeclaredFields()) {
            if(Objects.equals(f.getName(), name)) {
                return f;
            }
        }
        return getFieldRecursive(clazz.getSuperclass(), name);
    }

    private <X> Optional<DataType> getDataType(Attribute<? super X, ?> attribute) {
        Class<?> javaType = attribute.getJavaType();
        switch (attribute.getPersistentAttributeType()) {
            case BASIC:
                return Optional.of(DataType.valueOf(javaType));
            case ONE_TO_ONE:
            case MANY_TO_ONE:
            case ONE_TO_MANY:
            case MANY_TO_MANY:
                return Optional.of(DataType.LINK);
            case EMBEDDED:
                break;
            case ELEMENT_COLLECTION:
                break;
        }
        return Optional.empty();
    }

    public List<FieldDescriptor> findFields(String entityName) {
        return metaDelegateRepository.findFieldDescriptorsByEntity(entityName);
    }

    public FieldDescriptor createFiled(String entityName, FieldDescriptor fieldDescriptor) {
        Assert.hasText(fieldDescriptor.getName(), "name can not be empty");

        Assert.isTrue(!metaDelegateRepository.findFieldDescriptorByEntityAndName(entityName, fieldDescriptor.getName()).isPresent(),
                "duplicated field name: " + fieldDescriptor.getName());
        Assert.isTrue(fieldDescriptor.getDataType().isExtensible(), fieldDescriptor.getDataType() + " can not be used in extension field");
        Assert.isTrue(fieldDescriptor.getDataType().isIndexable() || !fieldDescriptor.isIndexed(), fieldDescriptor.getDataType() + " can not be indexed");

        fieldDescriptor.setExtension(true);
        fieldDescriptor.setEntity(entityName);
        if(fieldDescriptor.getLabel() == null) {
            fieldDescriptor.setLabel(fieldDescriptor.getName());
        }
        fieldDescriptor.setDomainField(findNextPhysicalField(entityName, fieldDescriptor.getDataType(), fieldDescriptor.isIndexed()));
        validate(fieldDescriptor);
        return metaDelegateRepository.save(fieldDescriptor, false);
    }

    private void validate(FieldDescriptor fieldDescriptor) {
        if(fieldDescriptor.getDataType() == DataType.ENUM) {
            String enumRef = fieldDescriptor.getEnumRef();
            Assert.hasText(enumRef, "enumRef can not be empty if dataType ENUM");
            EnumDescriptor enumDescriptor = metaDelegateRepository.findEnumByName(enumRef)
                    .orElseThrow(() -> new IllegalArgumentException(enumRef + " is not validated enumRef"));
            Assert.isTrue(!enumDescriptor.isDisable(), "enum is disabled ");
        }
    }

    public void removeField(String entityName, String fieldName) {
        FieldDescriptor fieldDescriptor = metaDelegateRepository.getFieldDescriptorByEntityAndName(entityName, fieldName);
        Assert.isTrue(fieldDescriptor.isExtension(), fieldName  + " can not be deleted");
        metaDelegateRepository.delete(fieldDescriptor);
    }

    private String findNextPhysicalField(String entityName, DataType dataType, boolean indexed) {
        Set<String> existedFields = this.metaDelegateRepository.findFieldDescriptorsByEntity(entityName)
                .stream()
                .filter(fieldDescriptor -> Objects.equals(fieldDescriptor.getDataType(), dataType)
                        && Objects.equals(fieldDescriptor.isIndexed(), indexed))
                .map(FieldDescriptor::getDomainField)
                .collect(Collectors.toSet());

        Set<String> candidatesFields = ExtensionFields.getCandidateFields(dataType, indexed);

        candidatesFields.removeAll(existedFields);
        return candidatesFields.stream()
                .min(String::compareTo)
                .orElseThrow(() -> new IllegalStateException((indexed? "indexd ": "") + dataType + " can not be used as extension field's dataType on current entity any more."));

    }

    public Optional<FieldDescriptor> getField(Class domainType, String property) {
        if(!EmsEntity.class.isAssignableFrom(domainType)) {
            return Optional.empty();
        }
        EntityType entity = entityManager.getMetamodel().entity(domainType);
        return metaDelegateRepository.findFieldDescriptorByEntityAndName(entity.getName(), property);
    }

    public FieldDescriptor getField(String entityName, String property) {
        return metaDelegateRepository.findFieldDescriptorByEntityAndName(entityName, property)
                .orElseThrow(() -> new ResourceNotFoundException(entityName + "." + property));
    }

    public List<EventHandler> findEventHandlers(String entityName) {
        return metaDelegateRepository.findEventHandlersByEntity(entityName);
    }

    public EventHandler getEventHandler(String entityName, String handlerName) {
        return metaDelegateRepository.findEventHandlerByEntityAndName(entityName, handlerName)
                .orElseThrow(() -> new ResourceNotFoundException(entityName + "." + handlerName));
    }

    public EventHandler attachEventHandler(String entityName, EventHandler eventHandler) {
        eventHandler.setEntity(entityName);
        return metaDelegateRepository.save(eventHandler, false);
    }

    public void detachEventHandler(String entityName, String handlerName) {
        EventHandler eventHandler = metaDelegateRepository.findEventHandlerByEntityAndName(entityName, handlerName)
                .orElseThrow(() -> new ResourceNotFoundException(entityName + "." + handlerName));
        metaDelegateRepository.delete(eventHandler);
    }

    public List<FieldDescriptor> findFields(Class<? extends EmsEntity> aClass) {
        EntityDescriptor entityDescriptor = getEntity(aClass);
        return metaDelegateRepository.findFieldDescriptorsByEntity(entityDescriptor.getName());
    }

    public List<EnumDescriptor> getAllEnums() {
        return metaDelegateRepository.findAllEnums().stream()
                .map(this::sortValues)
                .collect(Collectors.toList());
    }

    public EnumDescriptor getEnumValue(String enumName) {
        return metaDelegateRepository.findEnumByName(enumName)
                .map(this::sortValues)
                .orElseThrow(() -> new ResourceNotFoundException(enumName));
    }

    public EnumDescriptor createEnum(EnumDescriptor enumDescriptor) {
        enumDescriptor.setExtension(true);
        return sortValues(metaDelegateRepository.createEnum(enumDescriptor, false));
    }

    public EnumDescriptor updateEnum(String enumName, EnumDescriptor enumDescriptor) {
        EnumDescriptor enumDescriptorExisted = metaDelegateRepository.findEnumByName(enumName)
                .orElseThrow(() -> new ResourceNotFoundException(enumName));
        Assert.isTrue(enumDescriptor.isExtension(),"OOB enum can not be updated");
        Assert.isTrue(Objects.equals(enumName, enumDescriptor.getName()),"enum.name can not be updated");
        BeanUtils.copyProperties(enumDescriptor, enumDescriptorExisted, MetaEntity.Fields.id);
        return sortValues(enumDescriptorExisted);
    }

    private EnumDescriptor sortValues(EnumDescriptor enumDescriptor) {
        enumDescriptor.getValues().sort(Comparator.comparing(EnumValue::getSequence));
        return enumDescriptor;
    }


    @Autowired
    private PlatformTransactionManager platformTransactionManager;


    /**
     * 获取或创建指定 Class 的Meta Cache. 并在当前事物的边界内缓存该 Meta Cache
     * @param clazz
     * @return
     */
    public MetaCache getOrCreateMetaCache(Class<? extends EmsEntity> clazz) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        return transactionTemplate.execute(ts -> {
            if(!TransactionSynchronizationManager.hasResource(TECH_KUIPERBELT_EMS_META_CACHE_MAP)) {
                initTransactionMetaCacheMap();
            }
            Map<Class<? extends EmsEntity>, MetaCache> metaCacheMap =
                    (Map<Class<? extends EmsEntity>, MetaCache>) TransactionSynchronizationManager.getResource(TECH_KUIPERBELT_EMS_META_CACHE_MAP);
            if(!metaCacheMap.containsKey(clazz)) {
                metaCacheMap.put(clazz, createMetaCache(clazz));
            }
            return metaCacheMap.get(clazz);
        });
    }

    private MetaCache createMetaCache(Class<? extends EmsEntity> clazz) {
        log.trace("Create MetaCache for " + clazz);
        EntityType<? extends EmsEntity> entityType = this.entityManager.getMetamodel().entity(clazz);
        EntityDescriptor entityDescriptor = this.getEntity(clazz);
        List<FieldDescriptor> fields = this.findFields(clazz);
        List<EventHandler> eventHandlers = this.findEventHandlers(entityType.getName());
        Map<String, EnumDescriptor> enumDescriptorMap = fields.stream()
                .filter(fieldDescriptor -> fieldDescriptor.getDataType() == DataType.ENUM)
                .map(FieldDescriptor::getEnumRef)
                .collect(Collectors.toMap(Function.identity(), this::getEnumValue));

        return new MetaCache(clazz, entityDescriptor, fields, eventHandlers, enumDescriptorMap);
    }

    private void initTransactionMetaCacheMap() {
        //register clean up hook
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                TransactionSynchronizationManager.unbindResource(TECH_KUIPERBELT_EMS_META_CACHE_MAP);
            }
        });
        //init HashMap
        TransactionSynchronizationManager.bindResource(TECH_KUIPERBELT_EMS_META_CACHE_MAP, new HashMap<>());
    }

}
