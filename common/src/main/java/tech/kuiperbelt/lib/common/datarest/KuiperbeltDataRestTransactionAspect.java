package tech.kuiperbelt.lib.common.datarest;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.NoRollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;

import java.util.Arrays;

/**
 * Wrap all spring data rest event handler in one transaction.，用以实现更为细粒度的事物管理
 */
@Aspect
@Slf4j
public class KuiperbeltDataRestTransactionAspect {

    private final PlatformTransactionManager transactionManager;

    public KuiperbeltDataRestTransactionAspect(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Pointcut("execution(* org.springframework.data.rest.webmvc.*Controller.*(..))")
    public void aroundDataRestCall(){}

    @Around("aroundDataRestCall()")
    public Object aroundDataRestCall(ProceedingJoinPoint joinPoint) throws Throwable {
        RuleBasedTransactionAttribute def = new RuleBasedTransactionAttribute();
        def.setName("kuiperbelt-data-rest-transaction");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.getRollbackRules().add(new NoRollbackRuleAttribute(NonRollBackRuntimeException.class));
        def.getRollbackRules().add(new NoRollbackRuleAttribute(NonRollBackException.class));
        TransactionStatus status = transactionManager.getTransaction(def);
        Object[] args = joinPoint.getArgs();
        try {
            log.trace("kuiperbelt-data-rest 事物开始，调用参数{}, 事物属性 {}", Arrays.toString(args), toString(status));
            Object result = joinPoint.proceed();
            transactionManager.commit(status);
            log.debug("kuiperbelt-data-rest 事物正常结束，提交. 事物属性 {}", toString(status));
            return result;
        } catch (Throwable ex) {
            if(ex instanceof ResourceNotFoundException) {
                log.warn("ResourceNotFoundException(Entity)资源找不到, 调用参数{}, 事物属性 {}", Arrays.toString(args), toString(status));
            } else {
                log.warn("kuiperbelt-data-rest 事物执行过程中发生异常, 调用参数{}, 事物属性 {}", Arrays.toString(args), toString(status), ex);
            }

            if(ex instanceof NonRollBackRuntimeException
                    || ex instanceof NonRollBackException ) {
                if(status.isCompleted()) {
                    log.warn("kuiperbelt-data-rest 异常已经完成，无须再次提交");
                } else {
                    log.warn("kuiperbelt-data-rest 异常是非回滚异常（NonRollBackException），提交当前事物");
                    transactionManager.commit(status);
                }
            } else {
                if(status.isCompleted()) {
                    log.warn("kuiperbelt-data-rest 异常已经完成，无须再次回滚");
                } else {
                    if(ex instanceof ResourceNotFoundException) {
                        log.debug("出错信息{}, 出错的请求payload为{}",ex.getMessage(), Arrays.toString(args), ex);
                    } else {
                        log.warn("出错信息{}, 出错的请求payload为{}",ex.getMessage(), Arrays.toString(args), ex);
                    }
                    log.warn("kuiperbelt-data-rest 事物回滚");
                    transactionManager.rollback(status);
                }
            }
            throw ex;
        }
    }

    private String toString(TransactionStatus status) {
        return "isNew: " +  status.isNewTransaction() +  ", hasSavePoint: " + status.hasSavepoint() + ", isCompleted: " + status.isCompleted()
                + ", isRollbackOnly: " + status.isRollbackOnly() + "";
    }
}
