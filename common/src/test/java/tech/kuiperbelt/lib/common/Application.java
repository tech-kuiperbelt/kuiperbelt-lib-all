package tech.kuiperbelt.lib.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import tech.kuiperbelt.lib.common.datarest.EnableKuiperbeltDataRestConfig;
import tech.kuiperbelt.lib.common.jpa.BaseRepositoryImplement;
import tech.kuiperbelt.lib.common.jpa.EnableKuiperbeltJapConfig;
import tech.kuiperbelt.lib.common.jpa.audit.EnableKuiperbeltJapAuditConfig;

@EnableKuiperbeltDataRestConfig
@EnableKuiperbeltJapAuditConfig
@EnableKuiperbeltJapConfig
@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImplement.class)
@EntityScan(basePackageClasses = {Application.class})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
