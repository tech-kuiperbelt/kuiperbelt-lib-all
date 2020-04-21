package tech.kuiperbelt.lib.common.datarest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.rest.core.annotation.*;
import tech.kuiperbelt.lib.common.jpa.BaseEntity;


/**
 * 将spring-date-rest 的 CRUD 操作 用log输出
 */

@Slf4j
@RepositoryEventHandler
public class LogHandler {

    @Order(Integer.MIN_VALUE)
    @HandleBeforeCreate
    public void beforeCreate(BaseEntity baseEntity) {
        log.info("开始新增 {} {}, id：{}, bizKey: {}", baseEntity.getClass().getSimpleName(), baseEntity.getShortName(), baseEntity.getId(), baseEntity.getBizKey());
        log.debug("开始新增 {}", baseEntity);
    }

    @Order(Integer.MIN_VALUE)
    @HandleAfterCreate
    public void afterCreate(BaseEntity baseEntity) {
        log.info("结束新增 {} {}, id：{}, bizKey: {}", baseEntity.getClass().getSimpleName(), baseEntity.getShortName(), baseEntity.getId(), baseEntity.getBizKey());
        log.debug("结束新增 {}", baseEntity);
    }

    @Order(Integer.MIN_VALUE)
    @HandleBeforeSave
    public void beforeSave(BaseEntity baseEntity) {
        log.info("开始保存 {} {}, id：{}, bizKey: {}", baseEntity.getClass().getSimpleName(), baseEntity.getShortName(), baseEntity.getId(), baseEntity.getBizKey());
        log.debug("开始保存 {}", baseEntity);
    }

    @Order(Integer.MIN_VALUE)
    @HandleAfterSave
    public void afterSave(BaseEntity baseEntity) {
        log.info("结束保存 {} {}, id：{}, bizKey: {}", baseEntity.getClass().getSimpleName(), baseEntity.getShortName(), baseEntity.getId(), baseEntity.getBizKey());
        log.debug("结束保存 {}", baseEntity);
    }

    @Order(Integer.MIN_VALUE)
    @HandleBeforeDelete
    public void beforeDelete(BaseEntity baseEntity) {
        log.info("开始删除 {} {}, id：{}, bizKey: {}", baseEntity.getClass().getSimpleName(), baseEntity.getShortName(), baseEntity.getId(), baseEntity.getBizKey());
        log.debug("开始删除 {}", baseEntity);
    }

    @Order(Integer.MIN_VALUE)
    @HandleAfterDelete
    public void afterDelete(BaseEntity baseEntity) {
        log.info("结束删除 {} {}, id：{}, bizKey: {}", baseEntity.getClass().getSimpleName(), baseEntity.getShortName(), baseEntity.getId(), baseEntity.getBizKey());
        log.debug("结束删除 {}", baseEntity);
    }
}
