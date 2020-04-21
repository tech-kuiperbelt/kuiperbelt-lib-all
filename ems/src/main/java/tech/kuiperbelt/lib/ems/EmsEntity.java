package tech.kuiperbelt.lib.ems;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import groovy.lang.DelegatingMetaClass;
import groovy.lang.GroovyInterceptable;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassImpl;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.envers.Audited;
import org.springframework.util.Assert;
import tech.kuiperbelt.lib.common.jpa.BaseEntity;

import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * EMS 可扩展类的 Base
 */
@MappedSuperclass
@FieldNameConstants
public class EmsEntity extends BaseEntity implements GroovyInterceptable {

    @JsonIgnore
    @Transient
    private MetaClass metaClass;

    @JsonIgnore
    @Transient
    private MetaCache metaCache;

    @JsonIgnore
    MetaCache getMetaCache() {
        return metaCache;
    }

    @Audited
    @JsonIgnore
    @Embedded
    private ExtensionFields ext;

    /**
     * default constructor
     */
    public EmsEntity() {
        this.ext = new ExtensionFields();
        this.metaClass = new MetaClassImpl(this.getClass());
        this.metaClass.initialize();
        MetaService.getInstance().ifPresent(this::initializeExtFieldsMeta);
    }

    private void initializeExtFieldsMeta(MetaService metaService) {
        metaCache = metaService.getOrCreateMetaCache(this.getClass());
    }



    @Override
    public Object invokeMethod(String name, Object args) {
        return this.metaClass.invokeMethod(this, name, args);
    }

    /**
     * Used as Groovy dynamic property getter
     * @param propertyName
     * @return
     */
    @Override
    public Object getProperty(String propertyName) {
        if(this.metaClass.hasProperty(this, propertyName) != null) {
            return this.metaClass.getProperty(this, propertyName);
        } else {
            Assert.notNull(metaCache, "extension fields must be initialized");
            if(metaCache.getExtensionFieldDescriptorMap().containsKey(propertyName)) {
                return initIfNull(this.ext).getProperty(metaCache.getExtensionFieldDescriptorMap().get(propertyName));
            } else {
                return null;
            }
        }
    }

    /**
     * Used as both jackson json deserialize and Groovy dynamic property setter
     * @param propertyName
     * @param newValue
     */
    @JsonAnySetter
    @Override
    public void setProperty(String propertyName, Object newValue) {
        if(this.metaClass.hasProperty(this, propertyName) != null) {
            this.metaClass.setProperty(this, propertyName, newValue);
        } else {
            Assert.notNull(metaCache, "extension fields must be initialized");
            if(metaCache.getExtensionFieldDescriptorMap().containsKey(propertyName)) {
                FieldDescriptor fieldDescriptor = metaCache.getExtensionFieldDescriptorMap().get(propertyName);
                validate(fieldDescriptor, newValue);
                initIfNull(this.ext).setProperty(fieldDescriptor, newValue);
            }
        }
    }

    private void validate(FieldDescriptor fieldDescriptor, Object newValue) {
        if(newValue == null) {
            return;
        }

        if(fieldDescriptor.getDataType() == DataType.ENUM) {
            EnumDescriptor enumDescriptor  = metaCache.getEnumDescriptorMap().get(fieldDescriptor.getEnumRef());
            boolean match = enumDescriptor.getValues()
                    .stream()
                    .anyMatch(enumValue -> Objects.equals(newValue, enumValue.getValue()));
            if(!match) {
                throw new IllegalArgumentException(newValue + " is not valid " + fieldDescriptor.getEnumRef());
            }
        }
    }

    private ExtensionFields initIfNull(ExtensionFields ext) {
        if(ext == null) {
            this.ext = new ExtensionFields();
        }
        return this.ext;
    }

    /**
     * Used as Jackson json serialize
     * @return
     */
    @Transient
    @JsonAnyGetter
    public Map<String, Object> extFieldsToMap() {
        return metaCache.getExtensionFieldDescriptorMap().values()
                .stream()
                .map(fieldDescriptor -> Pair.of(fieldDescriptor.getName(), initIfNull(this.ext).getProperty(fieldDescriptor)))
                .filter(pair -> pair.getRight()!= null)
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }


    @Override
    public MetaClass getMetaClass() {
        return this.metaClass;
    }

    @Override
    public void setMetaClass(MetaClass metaClass) {
        this.metaClass = new DelegatingMetaClass(metaClass);
    }

    /**
     * Used by PUT update
     */
    public void cleanExtFieldsValue() {
        this.ext.cleanAllValues();
    }
}
