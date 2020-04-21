package tech.kuiperbelt.lib.ems;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Enum Descriptor
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnumDescriptor extends MetaEntity {

    private String name;

    private boolean extension;

    private boolean disable;

    @Embedded
    @ElementCollection
    private List<EnumValue> values;

    public static EnumDescriptor buildFromJavaEnum(Class<? extends Enum<?>> enumClass) {
        EnumDescriptor enumDescriptor = EnumDescriptor.builder()
                .name(enumClass.getName())
                .extension(false)
                .values(Stream.of(enumClass.getEnumConstants())
                        .map(e -> EnumValue.builder()
                                .label(e.name())
                                .value(e.name())
                                .sequence(e.ordinal())
                                .build())
                        .collect(Collectors.toList())
                )
                .build();
        return enumDescriptor;
    }
}
