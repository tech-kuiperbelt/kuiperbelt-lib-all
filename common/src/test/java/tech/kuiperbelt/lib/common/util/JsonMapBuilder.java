package tech.kuiperbelt.lib.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsonMapBuilder {
    private Map<String, Object> jsonMap = new LinkedHashMap<>();
    public JsonMapBuilder add(String name, Object value) {
        jsonMap.put(name, value);
        return this;
    }
    public Map<String, Object> build() {
        return jsonMap;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(jsonMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
