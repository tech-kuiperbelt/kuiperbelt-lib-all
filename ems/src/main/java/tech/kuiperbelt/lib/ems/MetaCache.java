package tech.kuiperbelt.lib.ems;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * EmsEntity Meta Cache， 每个Entity  对应构造它的时候的Meta，并贯穿整个生命周期
 */
@Getter
public class MetaCache {
    private Class<? extends EmsEntity> clazz;

    private final EntityDescriptor entityDescriptor;

    private final Map<String, FieldDescriptor> fieldDescriptorMap;

    private final Map<String, EnumDescriptor> enumDescriptorMap;

    private final List<EventHandler> eventHandlerList;

    private Map<EntityEvent.Type, Map<String, Script>> eventTypeMap = null;

    public Map<EntityEvent.Type, Map<String, Script>> getEventTypeMap() {
        return Objects.requireNonNull(eventTypeMap, "MetaCache.parse should be call first");
    }

    public void setEventTypeMap(Map<EntityEvent.Type, Map<String, Script>> eventTypeMap) {
        this.eventTypeMap = eventTypeMap;
    }

    public MetaCache(Class<? extends EmsEntity> clazz,
                     EntityDescriptor entityDescriptor,
                     List<FieldDescriptor> fieldDescriptorList,
                     List<EventHandler> eventHandlerList,
                     Map<String, EnumDescriptor> enumDescriptorMap) {
        this.clazz = clazz;
        this.entityDescriptor = entityDescriptor;
        this.fieldDescriptorMap = fieldDescriptorList.stream()
                .collect(Collectors.toMap(FieldDescriptor::getName, Function.identity()));
        this.eventHandlerList = eventHandlerList.stream().collect(Collectors.toList());

        this.enumDescriptorMap = new HashMap<>(enumDescriptorMap);
    }

    public void parse(GroovyShell groovyShell) {
        if(eventTypeMap == null) {
            synchronized (this) {
                if(eventTypeMap == null) {
                    eventTypeMap = new EnumMap<EntityEvent.Type, Map<String, Script>>(EntityEvent.Type.class);
                    Stream.of(EntityEvent.Type.values()).forEach(type ->
                        eventTypeMap.put(type, eventHandlerList.stream()
                                .filter(eventHandler -> eventHandler.getType() == type)
                                .sorted(Comparator.comparing(EventHandler::getRanking))
                                .collect(Collectors.toMap(EventHandler::getName,
                                        eventHandler ->  groovyShell.parse(eventHandler.getScript()),
                                        (a, b) -> a,
                                        LinkedHashMap::new)))
                    );
                }
            }
        }
    }

    public Map<String, FieldDescriptor> getExtensionFieldDescriptorMap() {
        return this.fieldDescriptorMap.values().stream()
                .filter(FieldDescriptor::isExtension)
                .collect(Collectors.toMap(FieldDescriptor::getName, Function.identity()));
    }

}
