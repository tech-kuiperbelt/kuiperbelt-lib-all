package tech.kuiperbelt.lib.ems;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Trigger Entity CURD Event
 */
@Slf4j
@Service
@RepositoryEventHandler
public class EntityEventTriggerService {

    private GroovyShell groovyShell;

    public static final String HANDLER = "handler";
    @Autowired
    private MetaService metaService;

    @Qualifier("entityEventThreadPoolTaskExecutor")
    @Autowired
    private Executor asyncExecutor;


    public EntityEventTriggerService() {
        GroovyClassLoader loader = new GroovyClassLoader(EntityEventTriggerService.class.getClassLoader());
        Binding binding = new Binding();
        binding.setProperty("log", log);
        this.groovyShell = new GroovyShell(loader, binding);
    }

    @Transactional
    @HandleBeforeCreate
    public void beforeCreate(EmsEntity emsEntity) {
        emsEntity.getMetaCache().parse(groovyShell);
        runValidationHandler(findEventHandlers(emsEntity, EntityEvent.Type.VALIDATE_CREATE), EntityEvent.builder()
                .type(EntityEvent.Type.VALIDATE_CREATE)
                .current(emsEntity)
                .build());
        runEventHandler(findEventHandlers(emsEntity, EntityEvent.Type.BEFORE_CREATE), EntityEvent.builder()
                .type(EntityEvent.Type.BEFORE_CREATE)
                .current(emsEntity)
                .build());
    }

    @Transactional
    @HandleAfterCreate
    public void afterCreate(EmsEntity emsEntity) {
        runEventHandler(findEventHandlers(emsEntity, EntityEvent.Type.AFTER_CREATE), EntityEvent.builder()
                .type(EntityEvent.Type.AFTER_CREATE)
                .current(emsEntity)
                .build());

        postExecAsync(emsEntity, EntityEvent.Type.POST_CREATE);
    }


    @HandleBeforeSave
    public void beforeUpdate(EmsEntity emsEntity) {
        emsEntity.getMetaCache().parse(groovyShell);
        runValidationHandler(findEventHandlers(emsEntity, EntityEvent.Type.VALIDATE_UPDATE), EntityEvent.builder()
                .type(EntityEvent.Type.VALIDATE_UPDATE)
                .current(emsEntity)
                .build());
        runEventHandler(findEventHandlers(emsEntity, EntityEvent.Type.BEFORE_UPDATE), EntityEvent.builder()
                .type(EntityEvent.Type.BEFORE_UPDATE)
                .current(emsEntity)
                .build());
    }


    @HandleAfterSave
    public void afterUpdate(EmsEntity emsEntity) {
        runEventHandler(findEventHandlers(emsEntity, EntityEvent.Type.AFTER_UPDATE), EntityEvent.builder()
                .type(EntityEvent.Type.AFTER_UPDATE)
                .current(emsEntity)
                .build());

        postExecAsync(emsEntity, EntityEvent.Type.POST_UPDATE);
    }

    @HandleBeforeDelete
    public void beforeDelete(EmsEntity emsEntity) {
        emsEntity.getMetaCache().parse(groovyShell);
        runValidationHandler(findEventHandlers(emsEntity, EntityEvent.Type.VALIDATE_DELETE), EntityEvent.builder()
                .type(EntityEvent.Type.VALIDATE_DELETE)
                .current(emsEntity)
                .build());
        runEventHandler(findEventHandlers(emsEntity, EntityEvent.Type.BEFORE_DELETE), EntityEvent.builder()
                .type(EntityEvent.Type.BEFORE_DELETE)
                .current(emsEntity)
                .build());
    }

    @HandleAfterDelete
    public void afterDelete(EmsEntity emsEntity) {
        runEventHandler(findEventHandlers(emsEntity, EntityEvent.Type.AFTER_DELETE), EntityEvent.builder()
                .type(EntityEvent.Type.AFTER_DELETE)
                .current(emsEntity)
                .build());

        postExecAsync(emsEntity, EntityEvent.Type.POST_DELETE);
    }

    private void runEventHandler(Map<String, Script> eventHandlers, EntityEvent entityEvent) {
        for(String name: eventHandlers.keySet()) {
            eventHandlers.get(name).invokeMethod(HANDLER, entityEvent);
        }
    }

    private void runValidationHandler(Map<String, Script> validateEventHandlers, EntityEvent entityEvent) {
        for(String name: validateEventHandlers.keySet()) {
            Boolean result = (Boolean) validateEventHandlers.get(name).invokeMethod(HANDLER, entityEvent);
            if(result != null && result == false) {
                throw new IllegalArgumentException(name);
            }
        }
    }

    private void postExecAsync(EmsEntity emsEntity, EntityEvent.Type eventType) {
        asyncExecutor.execute(() -> {
            runEventHandler(findEventHandlers(emsEntity, eventType), EntityEvent.builder()
                    .type(eventType)
                    .current(emsEntity)
                    .build());
        });
    }

    private Map<String, Script> findEventHandlers(EmsEntity emsEntity, EntityEvent.Type eventType) {
        return emsEntity.getMetaCache().getEventTypeMap().get(eventType);
    }

}
