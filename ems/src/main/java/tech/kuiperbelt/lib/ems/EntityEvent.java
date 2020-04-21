package tech.kuiperbelt.lib.ems;

import lombok.*;

/**
 * Entity CURD Event
 */
@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EntityEvent {
    private Type type;
    private EmsEntity current;
    private EmsEntity old;



    public enum Type {
        VALIDATE_CREATE,
        BEFORE_CREATE,
        AFTER_CREATE,
        POST_CREATE,

        VALIDATE_UPDATE,
        BEFORE_UPDATE,
        AFTER_UPDATE,
        POST_UPDATE,

        VALIDATE_DELETE,
        BEFORE_DELETE,
        AFTER_DELETE,
        POST_DELETE
    }

}
