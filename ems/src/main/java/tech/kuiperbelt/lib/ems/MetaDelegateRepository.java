package tech.kuiperbelt.lib.ems;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import tech.kuiperbelt.lib.common.jpa.SnowflakeIdWorker;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

import static tech.kuiperbelt.lib.ems.MetaEntity.OOB_CREATED_BY;
import static tech.kuiperbelt.lib.ems.MetaEntity.OOB_UPDATED_BY;

/**
 * Meta Data Repository
 * Delegate call to real JAP Repository (for Customize Meta Data) and memory cache (for OOB Meta Data)
 * and Merge result for fetch operator
 */
@Transactional
@Service
public class MetaDelegateRepository {

    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    private FieldDescriptorRepository fieldDescriptorRepository;

    @Autowired
    private EventHandlerRepository eventHandlerRepository;

    @Autowired
    private EnumDescriptorRepository enumDescriptorRepository;

    private Map<String, EntityDescriptor> oobEntityDescriptorMap = new HashMap<>();

    private Map<String, Map<String, FieldDescriptor>> oobFieldDescriptorMap = new HashMap<>();

    private Map<String, EnumDescriptor> oobEnumDescriptorMap = new HashMap<>();

    private static <T extends MetaEntity> T fillFieldsForOobMeta(T t) {
        LocalDateTime now = LocalDateTime.now();
        t.setCreatedBy(OOB_CREATED_BY);
        t.setCreatedTime(now);
        t.setLastUpdatedBy(OOB_UPDATED_BY);
        t.setLastUpdatedTime(now);
        return t;
    }

    public List<EntityDescriptor> findAllEntityDescriptors() {
        return new ArrayList<>(oobEntityDescriptorMap.values());
    }

    public Optional<EntityDescriptor> findEntityDescriptorByName(@Param("name") String name) {
        return Optional.ofNullable(oobEntityDescriptorMap.get(name));
    }

    public EntityDescriptor getEntityDescriptorByName(@Param("name") String name) {
        return this.findEntityDescriptorByName(name)
                .orElseThrow(() -> new EntityNotFoundException(name));
    }


    public EntityDescriptor save(EntityDescriptor entityDescriptor, boolean isOob) {
        Assert.isTrue(isOob, "Only oob entityDescriptor is supported now.");
        if(isOob) {
            fillFieldsForOobMeta(entityDescriptor);
            entityDescriptor.setId(snowflakeIdWorker.nextId());
            oobEntityDescriptorMap.put(entityDescriptor.getName(), entityDescriptor);
        }
        return entityDescriptor;
    }

    public Optional<FieldDescriptor> findFieldDescriptorByEntityAndName(String name, String propertyName) {
        Optional<FieldDescriptor> oobFieldDescriptor = Optional.of(oobFieldDescriptorMap)
                .map(map -> map.get(name))
                .map(map -> map.get(propertyName));
        return oobFieldDescriptor.isPresent()? oobFieldDescriptor:
                fieldDescriptorRepository.findByEntityAndName(name, propertyName);
    }

    public FieldDescriptor save(FieldDescriptor fieldDescriptor, boolean isOob) {
        if(isOob) {
            fillFieldsForOobMeta(fieldDescriptor);
            if(!oobFieldDescriptorMap.containsKey(fieldDescriptor.getEntity())) {
                oobFieldDescriptorMap.put(fieldDescriptor.getEntity(), new HashMap<>());
            }
            fieldDescriptor.setId(snowflakeIdWorker.nextId());
            oobFieldDescriptorMap.get(fieldDescriptor.getEntity()).put(fieldDescriptor.getName(), fieldDescriptor);
            return fieldDescriptor;
        } else {
            return fieldDescriptorRepository.save(fieldDescriptor);
        }
    }

    public List<FieldDescriptor> findFieldDescriptorsByEntity(String entityName) {
        List<FieldDescriptor> result = new ArrayList<>(oobFieldDescriptorMap.getOrDefault(entityName, Collections.emptyMap()).values());
        List<FieldDescriptor> extensionFields = fieldDescriptorRepository.findByEntity(entityName);
        result.addAll(extensionFields);
        return result;
    }

    public FieldDescriptor getFieldDescriptorByEntityAndName(String entityName, String fieldName) {
        return findFieldDescriptorByEntityAndName(entityName, fieldName)
                .orElseThrow(() -> new ResourceNotFoundException(entityName + "." + fieldName));
    }

    public void delete(FieldDescriptor fieldDescriptor) {
        fieldDescriptorRepository.delete(fieldDescriptor);
    }


    public List<EventHandler> findEventHandlersByEntity(String entityName) {
        return eventHandlerRepository.findByEntity(entityName);
    }

    public EventHandler save(EventHandler eventHandler, boolean isOob) {
        Assert.isTrue(!isOob, "oob event is not supported yet.");
        if(isOob) {
            fillFieldsForOobMeta(eventHandler);
        }
        return eventHandlerRepository.save(eventHandler);
    }

    public Optional<EventHandler> findEventHandlerByEntityAndName(String entityName, String handlerName) {
        return eventHandlerRepository.findByEntityAndName(entityName, handlerName);
    }

    public void delete(EventHandler eventHandler) {
        eventHandlerRepository.delete(eventHandler);
    }

    public List<EnumDescriptor> findAllEnums() {
        ArrayList<EnumDescriptor> result = new ArrayList<>(oobEnumDescriptorMap.values());
        result.addAll(enumDescriptorRepository.findAll());
        return result;
    }

    public Optional<EnumDescriptor> findEnumByName(String enumName) {
        EnumDescriptor enumDescriptor = oobEnumDescriptorMap.get(enumName);
        if(enumDescriptor != null) {
            return Optional.of(enumDescriptor);
        } else {
            return enumDescriptorRepository.findByName(enumName);
        }
    }

    public EnumDescriptor createEnum(EnumDescriptor enumDescriptor, boolean isOob) {
        if(isOob) {
            fillFieldsForOobMeta(enumDescriptor);
            oobEnumDescriptorMap.put(enumDescriptor.getName(), enumDescriptor);
            return enumDescriptor;
        } else {
            return enumDescriptorRepository.save(enumDescriptor);
        }
    }
}
