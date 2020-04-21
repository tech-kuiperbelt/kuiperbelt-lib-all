package tech.kuiperbelt.lib.common.datarest;

/**
 * 非回滚， 捕获类型异常的基类, Exception
 * 当应用抛出 NonRollBackException 类型异常以及其子类异常时， Kuiperbelt data rest 不会回滚事物，而是会选择提交
 * 注意， 抛出异常的Service 如果使用申明式事物 @Transactional 属性， 需要同时把 抛出的异常 申明为 dontRollbackOn
 *      例如： @Transactional(dontRollbackOn = NonRollBackException.class)
 */
public class NonRollBackException extends Exception {
    public NonRollBackException() {
    }

    public NonRollBackException(String message) {
        super(message);
    }

    public NonRollBackException(String message, Throwable cause) {
        super(message, cause);
    }

    public NonRollBackException(Throwable cause) {
        super(cause);
    }

    public NonRollBackException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
