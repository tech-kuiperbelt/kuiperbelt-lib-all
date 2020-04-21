package tech.kuiperbelt.lib.common.datarest;

import lombok.Builder;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator of Entity HTTP Method
 */
public class CanNotValidator implements Validator {
    private final List<Class> classes;
    private final CURDOperation operation;

    @Builder
    public CanNotValidator(List<Class> classes, CURDOperation operation) {
        this.classes = new ArrayList<>(classes);
        this.operation = operation;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean supports(Class<?> aClass) {
        //noinspection unchecked
        return classes.stream().anyMatch(c -> c.isAssignableFrom(aClass));
    }

    @Override
    public void validate(Object o, Errors errors) {
        throw new UnsupportedOperationException(operation + " " +  o.getClass() + " is not allowed");
    }
}
