package tech.kuiperbelt.lib.ems;

import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.envers.Audited;

import javax.persistence.Embeddable;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Extension Field 的实现，作为一个Embedded Field 嵌入到 EmsEntity
 */
@Audited
@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldNameConstants
public class ExtensionFields {
    public static String IDX_FIELD_PREFIX = "idx";

    public static String STR_FIELD_PREFIX = "str";
    private String str0;
    private String str1;
    private String str2;
    private String str3;
    private String str4;
    private String str5;
    private String str6;
    private String str7;
    private String str8;
    private String str9;
    private String idxstr0;
    private String idxstr1;
    private String idxstr2;

    public static String INT_FIELD_PREFIX = "int";
    private Long int0;
    private Long int1;
    private Long int2;
    private Long int3;
    private Long int4;
    private Long idxint0;
    private Long idxint1;

    public static String FLOAT_FIELD_PREFIX = "flt";
    private Double flt0;
    private Double flt1;
    private Double flt2;
    private Double flt3;
    private Double flt4;

    public static String BOOLEAN_FIELD_PREFIX = "bln";
    private Boolean bln0;
    private Boolean bln1;
    private Boolean bln2;
    private Boolean bln3;
    private Boolean bln4;

    public static String DATE_FIELD_PREFIX = "dt";
    private LocalDate dt0;
    private LocalDate dt1;
    private LocalDate dt2;
    private LocalDate dt3;
    private LocalDate dt4;
    private LocalDate idxdt0;
    private LocalDate idxdt1;

    public static String DATETIME_FIELD_PREFIX = "tm";
    private LocalDateTime tm0;
    private LocalDateTime tm1;
    private LocalDateTime tm2;
    private LocalDateTime tm3;
    private LocalDateTime tm4;
    private LocalDateTime idxtm0;
    private LocalDateTime idxtm1;

    public static String ENUM_FIELD_PREFIX = "enm";
    private String enm0;
    private String enm1;
    private String enm2;
    private String enm3;
    private String enm4;
    private String idxenm0;
    private String idxenm1;


    public Object getProperty(FieldDescriptor fd) {
        try {
            return PropertyUtils.getProperty(this, fd.getDomainField());
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public void setProperty(FieldDescriptor fd, Object value) {
        try {
            PropertyUtils.setProperty(this, fd.getDomainField(), castTo(value, fd.getDataType()));
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object castTo(Object value, DataType dataType) {
        if(value == null) {
            return null;
        }
        switch (dataType) {
            case INT:
                return Long.valueOf(String.valueOf(value));
            case DATE:
                return LocalDate.parse(String.valueOf(value));
            case DATE_TIME:
                return LocalDateTime.parse(String.valueOf(value));
            default:
                return value;
        }
    }

    public static Set<String> getCandidateFields(DataType dataType, boolean isIndexed) {
        return Stream.of(PropertyUtils.getPropertyDescriptors(ExtensionFields.class))
                .map(PropertyDescriptor::getName)
                .filter(name -> isCandidateField(name, dataType, isIndexed))
                .collect(Collectors.toSet());
    }

    private static boolean isCandidateField(String physicalFieldName,  DataType dataType, boolean isIndexed) {
        String prefix = isIndexed? IDX_FIELD_PREFIX : "";
        switch (dataType) {
            case STRING:
                return physicalFieldName.startsWith(prefix + ExtensionFields.STR_FIELD_PREFIX);
            case BOOLEAN:
                return physicalFieldName.startsWith(prefix + ExtensionFields.BOOLEAN_FIELD_PREFIX);
            case FLOAT:
                return physicalFieldName.startsWith(prefix + ExtensionFields.FLOAT_FIELD_PREFIX);
            case INT:
                return physicalFieldName.startsWith(prefix + ExtensionFields.INT_FIELD_PREFIX);
            case DATE:
                return physicalFieldName.startsWith(prefix + ExtensionFields.DATE_FIELD_PREFIX);
            case DATE_TIME:
                return physicalFieldName.startsWith(prefix + ExtensionFields.DATETIME_FIELD_PREFIX);
            case ENUM:
                return physicalFieldName.startsWith(prefix + ExtensionFields.ENUM_FIELD_PREFIX);
            default:
                throw new IllegalArgumentException(dataType + " is not supported yet");
        }
    }

    public void cleanAllValues() {
        Stream.of(PropertyUtils.getPropertyDescriptors(ExtensionFields.class))
                .filter(propertyDescriptor -> propertyDescriptor.getWriteMethod() != null)
                .forEach(propertyDescriptor -> {
                    try {
                        PropertyUtils.setProperty(this, propertyDescriptor.getName(), null);
                    } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
