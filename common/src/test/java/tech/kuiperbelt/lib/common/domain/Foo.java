package tech.kuiperbelt.lib.common.domain;

import lombok.*;
import org.hibernate.envers.Audited;
import tech.kuiperbelt.lib.common.jpa.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Audited
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Foo extends BaseEntity {
    private String aaa;
    private Boolean bbb;
    private Integer ccc;
    private Long aLong;
    private Float aFloat;
    private BigDecimal aDecimal;
    private Double aDouble;
    private LocalDate aLocalDate;
    private LocalDateTime aLocalDateTime;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        ENABLED, DISABLED
    }
}
