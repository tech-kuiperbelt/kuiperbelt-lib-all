package tech.kuiperbelt.lib.ems;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@FieldNameConstants
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldDescriptor extends MetaEntity {

    @JsonIgnore
    @NotNull
    private String entity;

    private String name;

    private String label;

    @Enumerated(EnumType.STRING)
    private DataType dataType;

    private String domainField;

    private boolean indexed;

    private boolean extension;

    private String enumRef;

}
