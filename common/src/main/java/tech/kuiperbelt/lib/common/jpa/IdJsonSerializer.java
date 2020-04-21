package tech.kuiperbelt.lib.common.jpa;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * id Field output as format of "String" rather than Long, because
 *
     *  In JavaScript, all numbers are 64 bits floating point numbers.
     *  This means you can't represent in JavaScript all the Java longs.
     *  The size of the mantissa is about 53 bits, which means that your number,
     *  eg. 793548328091516928, can't be exactly represented as a JavaScript number.
 *
 *  see reference link: https://stackoverflow.com/questions/17320706/javascript-long-integer
 */
public class IdJsonSerializer extends StdScalarSerializer<Object> {

    private StringSerializer stringSerializer = new StringSerializer();

    protected IdJsonSerializer() {
        super(Long.class, false);
    }

    public boolean isEmpty(SerializerProvider prov, Object value) {
        String str = String.valueOf(value);
        return stringSerializer.isEmpty(prov, str);
    }

    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        stringSerializer.serialize(String.valueOf(value), gen, provider);
    }

    public final void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        stringSerializer.serializeWithType(String.valueOf(value), gen, provider, typeSer);
    }

    public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
        return stringSerializer.getSchema(provider, typeHint);
    }

    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
        stringSerializer.acceptJsonFormatVisitor(visitor, typeHint);
    }
}
