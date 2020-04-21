package tech.kuiperbelt.lib.ems;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * DataType Jackson 序列化输出， 只在 api /meta/data-types 中使用此序列化方式
 */
public class DataTypeSerializer extends StdSerializer<DataType> {
     
    public DataTypeSerializer() {
        this(null);
    }
   
    public DataTypeSerializer(Class<DataType> t) {
        super(t);
    }
 
    @Override
    public void serialize(
            DataType value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonProcessingException {

        jgen.writeStringField("name", value.name());
        jgen.writeBooleanField(DataType.Fields.indexable, value.isIndexable());
        jgen.writeBooleanField(DataType.Fields.extensible, value.isExtensible());
    }

    @Override
    public boolean isUnwrappingSerializer() {
        return true;
    }
}