package tech.kuiperbelt.lib.ems.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.envers.Audited;
import tech.kuiperbelt.lib.ems.EmsEntity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Audited
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Builder
public class Foo extends EmsEntity {
    private String name;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        ENABLED, DISABLED
    }
}
