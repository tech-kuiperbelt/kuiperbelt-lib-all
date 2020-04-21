package tech.kuiperbelt.lib.ems;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.rest.webmvc.json.DomainObjectReader;
import org.springframework.data.rest.webmvc.mapping.Associations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * fix Spring Data Rest 的PUT， Patch 方法无法支持有效的EMS 扩展字段
 * 在 PUT, POST 方法执行前，先将 扩展字段从Payload中移除，并缓存起来，然后在作 super.put, or super.patch 方法后，在将
 * 扩展字段复制到 Entity 上
 */
public class EmsDomainObjectReader extends DomainObjectReader {
    private PersistentEntities entities;
    public EmsDomainObjectReader(@NonNull PersistentEntities entities, @NonNull Associations associationLinks) {
        super(entities, associationLinks);
        this.entities = entities;
    }


    /**
     *对应与 HTTP PATCH Method
     * @param source
     * @param target
     * @param mapper
     * @param <T>
     * @return
     */
    @Override
    public <T> T read(InputStream source, T target, ObjectMapper mapper) {
        if(target instanceof EmsEntity) {
            try {
                ObjectNode root = (ObjectNode) mapper.readTree(source);
                Map<String, Object> extFields = preProcessingForExtFields(target, root);
                T result = super.read(new ByteArrayInputStream(mapper.writeValueAsBytes(root)), target, mapper);
                postProcessingForExtFields(extFields, (EmsEntity) result);
                return result;
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
            return super.read(source, target, mapper);
        }
    }


    /**
     * 对应与 HTTP PUT Method
     * remove extFields defined by customer temporary, after put set to these extFields by call putValueInExtFields
     * @param source
     * @param target
     * @param mapper
     * @param <T>
     * @return
     */
    @Override
    public <T> T readPut(ObjectNode source, T target, ObjectMapper mapper) {
        if(target instanceof EmsEntity) {
            Map<String, Object> extFields = preProcessingForExtFields(target, source);
            T result = super.readPut(source, target, mapper);
            //因为是Put, 所以先将所有的扩展字段清空
            ((EmsEntity)result).cleanExtFieldsValue();
            postProcessingForExtFields(extFields, (EmsEntity) result);
            return result;
        } else {
            return  super.readPut(source, target, mapper);
        }
    }

    private <T> Map<String, Object> preProcessingForExtFields(T target, ObjectNode root) {
        Map<String, Object> extFields = new HashMap<>();
        Class<? extends Object> type = target.getClass();
        entities.getPersistentEntity(type).ifPresent(persistentProperties -> {
            for (Iterator<Map.Entry<String, JsonNode>> i = root.fields(); i.hasNext(); ) {

                Map.Entry<String, JsonNode> entry = i.next();
                JsonNode child = entry.getValue();
                String fieldName = entry.getKey();

                if (persistentProperties.getPersistentProperty(fieldName) == null) {
                    switch (child.getNodeType()) {
                        case BOOLEAN:
                            extFields.put(fieldName, child.asBoolean());
                            break;

                        case STRING:
                            extFields.put(fieldName, child.asText());
                            break;

                        case NUMBER:
                            if(child.isLong() || child.isInt()) {
                                extFields.put(fieldName, child.asLong());
                            } else if(child.isFloat() || child.isDouble()){
                                extFields.put(fieldName, child.asDouble());
                            } else {
                                throw new IllegalArgumentException(child.getNodeType() + " is not supported yet");
                            }
                            break;

                        default:
                            throw new IllegalArgumentException(child.getNodeType() + " is not supported yet");
                    }
                    i.remove();
                    continue;
                }
            }
        });
        return extFields;
    }

    private <T> void postProcessingForExtFields(Map<String, Object> extFields, EmsEntity result) {
        EmsEntity emsEntity = result;
        for (String key : extFields.keySet()) {
            emsEntity.setProperty(key, extFields.get(key));
        }
    }
}
