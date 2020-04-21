package tech.kuiperbelt.lib.common.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.util.Assert;

import java.util.Optional;

import static org.springframework.boot.actuate.health.Status.*;

/**
 * The Utility Class to operate the HealIndicator of remote third-party service
 */
@Slf4j
public class ClientHealthIndicator {
    private volatile Status status = Status.UNKNOWN;
    private volatile Exception latestException;
    private final String name;

    public ClientHealthIndicator(String name) {
        Assert.hasText(name, "客户健康检查器必须有（英文/拼音）名称");
        this.name = name;
    }

    public void up() {
        if (status != Status.UP) {
            this.latestException = null;
            status = Status.UP;
            log.trace("Client {} is Up", name);
        }
    }

    public void outOfService(Exception e) {
        latestException = e;
        status = OUT_OF_SERVICE;
        log.trace("设置 health 标志 {}", OUT_OF_SERVICE);
    }

    public void down(Exception e) {
        latestException = e;
        status = DOWN;
        log.trace("设置 health 标志 {}", DOWN);
    }

    public Health health() {
        Health.Builder builder = Health.status(this.status);
        if(latestException != null) {
            builder.withException(latestException);
        }
        if(this.status == UNKNOWN) {
            log.trace("{} 未被调用， 状态未知", this.name);
            return builder.build();
        } else if(this.status == OUT_OF_SERVICE) {
            log.warn("Health 检查， 发现 {} OUT_OF_SERVICE， 最后一次异常，{}，", this.name, String.valueOf(latestException));
            return builder.withDetail("exception", Optional.ofNullable(latestException).map(Exception::getMessage).orElse("unknown"))
                    .build();
        } else if(this.status == DOWN) {
            log.warn("Health 检查， 发现 {} DOWN， 最后一次异常，{}，", this.name, String.valueOf(latestException));
            return builder
                    .withDetail("exception", Optional.ofNullable(latestException).map(Exception::getMessage).orElse("unknown"))
                    .build();
        }
        log.trace("{}  工作正常", this.name);
        return Health.up().build();
    }
}
