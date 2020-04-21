package tech.kuiperbelt.lib.ems;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import tech.kuiperbelt.lib.common.jpa.BaseEntity;

import javax.persistence.MappedSuperclass;

/**
 * 所有 Meta Entity 的 Base Class
 */
@Slf4j
@MappedSuperclass
@Getter
@Setter
@FieldNameConstants
public class MetaEntity extends BaseEntity {
    public final static String OOB_CREATED_BY = "SYSTEM";
    public final static String OOB_UPDATED_BY = "SYSTEM";
}
