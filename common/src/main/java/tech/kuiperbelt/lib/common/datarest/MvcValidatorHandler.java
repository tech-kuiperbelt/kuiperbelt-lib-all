package tech.kuiperbelt.lib.common.datarest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.event.AbstractRepositoryEventListener;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 在 save 之前 强制运行 validator
 */
public class MvcValidatorHandler  extends AbstractRepositoryEventListener {

    @Autowired(required = false)
    private Validator validator;


    @Override
    protected void onBeforeCreate(Object entity) {
        doValidate(entity);
    }

    @Override
    protected void onBeforeSave(Object entity) {
        doValidate(entity);
    }

    private void doValidate(Object entity) {
        if(validator != null) {
            Set<ConstraintViolation<Object>> results = validator.validate(entity);
            if(results.size() > 0) {
                String message = entity.getClass().getCanonicalName() + ": " + results.stream()
                        .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                        .collect(Collectors.joining(";"));
                throw new IllegalArgumentException(message);
            }
        }
    }


}
