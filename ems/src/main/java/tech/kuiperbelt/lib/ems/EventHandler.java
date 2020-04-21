package tech.kuiperbelt.lib.ems;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Event Handler
 */
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldNameConstants
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventHandler extends MetaEntity {

    @JsonIgnore
    @NotNull
    private String entity;

    @NotBlank
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EntityEvent.Type type;

    private int ranking;

    @NotBlank
    private String script;

}
