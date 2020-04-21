package tech.kuiperbelt.lib.ems;

import lombok.*;

import javax.persistence.Embeddable;

/**
 * Enum Value
 */
@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnumValue {

    private String label;

    private String value;

    private boolean disable;

    private int sequence;
}
