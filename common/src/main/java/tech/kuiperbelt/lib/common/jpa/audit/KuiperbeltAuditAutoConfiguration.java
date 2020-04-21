package tech.kuiperbelt.lib.common.jpa.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.EntityManager;
import java.security.Principal;
import java.util.Optional;

/**
 * Config Audit behaviors
 * 1 audit version field
 * 2 audit username
 */
@Configuration
@PropertySource("classpath:tech/kuiperbelt/lib/common/jpa/audit/auditor.properties")
public class KuiperbeltAuditAutoConfiguration implements AuditorAware<String> {

    @Bean("auditedQueryRepositoryImpl")
    public AuditRepository auditedQueryRepositoryImpl(EntityManager entityManager) {
        return new AuditRepositoryImpl(entityManager);
    }

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(authentication -> {
                    if(authentication.isAuthenticated()) {
                        Object principal = authentication.getPrincipal();
                        if(principal instanceof String) {
                            return (String)principal;
                        } else if(principal instanceof Principal) {
                            return ((Principal)principal).getName();
                        } else if (principal instanceof UserDetails) {
                            return ((UserDetails) principal).getUsername();
                        }
                    }
                    return "anonymousUser";
                });
    }
}
