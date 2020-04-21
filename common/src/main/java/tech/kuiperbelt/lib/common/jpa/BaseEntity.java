package tech.kuiperbelt.lib.common.jpa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.time.LocalDateTime;

@Audited
@Slf4j
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
@Getter
@Setter
@FieldNameConstants
abstract public class BaseEntity {
    public static final String DEFAULT_SHORT_NAME = "";
    @Id
    @JsonSerialize(using = IdJsonSerializer.class)
    @GenericGenerator(name = "long_by_uuid", strategy = "tech.kuiperbelt.lib.common.jpa.LongIdentifierGenerator")
    @GeneratedValue(generator = "long_by_uuid")
    private Long id;


    @Column(name = "created_time", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdTime;

    /**
     * 创建人
     */
    @Column(name = "created_by", updatable = false)
    @CreatedBy
    private String createdBy;

    @Column(name = "last_updated_time")
    @LastModifiedDate
    private LocalDateTime lastUpdatedTime;

    /**
     * 修改人
     */
    @Column(name = "last_updated_by")
    @LastModifiedBy
    private String lastUpdatedBy;

    @Version
    private Long version;

    /**
     * spring data rest 会忽略默认的version， 这里用只读属性currentVersion来代替
     * @return
     */
    public Long getCurrentVersion() {
        return this.version;
    }

    public void setCurrentVersion(Long version) {
        this.version = version;
    }


    @JsonIgnore
    @Transient
    private int hashCode = -1;

    /**
     * 如果hashCode的取值会发生变更，则对于Set， Map ，这些JAP中大量使用且以Hash为算法基础的类是不能正常工作
     * 因此，hashCode 在一个http 的一个request 的生命周期中应该保持不变值。
     * HashCode 只计算一次，来保证hashCode的不变性，一次保证对象被加入(hibernate, jpa implement)Map，Set之后的不变性 。
     * 计算时以业务主键为要素。对于已经持久化的对象，业务主键必定存在，且唯一，
     * 对于未被持久化的对象，也没有覆盖getBizKey，业务主键的默认entity id也不存在，这是按照对象原有的hashCode取。
     */
    @Override
    public final int hashCode() {
        if(hashCode == -1) {
            hashCode = this.getBizKey() == null? super.hashCode() : this.getBizKey().hashCode();
        }
        return hashCode;
    }

    /**
     * 以业务主键作为equals要素。对于已经持久化的对象，业务主键必定存在，且唯一，
     * 对于未被持久化的对象，也没有覆盖getBizKey，业务主键的默认entity id也不存在，这是仅当同一个（java）对象才被认为相等
     * @param o
     * @return
     */
    @Override
    public final boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof BaseEntity)) return false;
        BaseEntity other = (BaseEntity) o;
        return this.getBizKey() != null && this.getBizKey().equals(other.getBizKey());
    }

    /**
     * 返回当前实体的业务主键,子类可以覆盖，默认算法取entity id。
     * 业务主键就是实体（entity）在业务中的唯一标识，比如对Customer来说就是身份证号，对企业来说就是营业执照
     * 参见讨论见https://stackoverflow.com/questions/5031614/the-jpa-hashcode-equals-dilemma
     * bizKey取自业务键，是最好的策略，这样可以保证业务主键在任何时候都不会发生变更。无论实在持久化之前还是持久化之后。
     * bizKey主键取自主键，是可以接受的策略，这个策略会在对象第一次持久化前后失效，因为这个后，hashCode 不变，但是equal发生变更，
     * 理论上会到导致 对象equal 但hashCode没有强一致。但在实际中这个动作不会出现，因为Jap会把insert 动作延迟到事物最后一起执行。
     * 而此时不会有对集合的操作，如果这样的操作无法避免，那bizKey必须取值字不能为空的业务键。
     * @return
     */
    @JsonIgnore
    public Object getBizKey() {
        return this.id;
    }

    @SuppressWarnings("SameReturnValue")
    @JsonIgnore
    @Transient
    public String getShortName() {
        return DEFAULT_SHORT_NAME;
    }

    @Override
    public String toString() {
        String name = StringUtils.isEmpty(getShortName())? "BaseEntity" : getShortName();

        return name + " {" +
                "id=" + id +
                ", createdTime=" + createdTime +
                ", createdBy='" + createdBy + '\'' +
                ", lastUpdatedTime=" + lastUpdatedTime +
                ", lastUpdatedBy='" + lastUpdatedBy + '\'' +
                ", version=" + version +
                '}';
    }


    //*********************************** Log 信息处理 ***************************************

    @PrePersist
    public void onPrePersist() {
        log.debug("onPrePersist（即将新增） {} {}, id: {}, version: {}", this.getClass().getSimpleName(), this.getShortName(), this.id, this.version);
        log.trace("onPrePersist（即将新增） {} {}, {}", this.getClass().getSimpleName(), this.getShortName(), this);
    }

    @PreUpdate
    public void onPreUpdate() {
        log.debug("onPreUpdate（即将更新） {} {}, id: {}, version: {}", this.getClass().getSimpleName(), this.getShortName(), this.id, this.version);
        log.trace("onPreUpdate（即将更新） {} {}, {}", this.getClass().getSimpleName(), this.getShortName(), this);
    }

    @PreRemove
    public void onPreRemove() {
        log.debug("onPreRemove（即将删除） {} {}, id: {}, version: {}", this.getClass().getSimpleName(), this.getShortName(), this.id, this.version);
        log.trace("onPreRemove（即将删除） {} {}, {}", this.getClass().getSimpleName(), this.getShortName(), this);
    }
}