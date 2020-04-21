package tech.kuiperbelt.lib.common.jpa;

import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.RSQLOperators;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RSQL 的辅助类
 * RSQL 用来实现通用查寻， 参考资料见：https://github.com/jirutka/rsql-parser
 * 本方案的实现参考： https://www.baeldung.com/rest-api-search-language-rsql-fiql
 * @param <T>
 */
@Getter
public class GenericRSQLSpecification<T> implements Specification<T> {
    private final String property;
    private final ComparisonOperator operator;
    private final List<String> arguments;

    public GenericRSQLSpecification(String property, ComparisonOperator operator, List<String> arguments) {
        this.property = property;
        this.operator = operator;
        this.arguments = arguments;
    }

    /**
     * 根据不同类型 处理 比较操作符
     * @param root
     * @param criteriaQuery
     * @param criteriaBuilder
     * @return
     */
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        List<Object> args = castArguments(root);
        Object argument = args.get(0);
        if(Objects.equals(operator, RSQLOperators.EQUAL)) {
            if(argument instanceof String) {
                //noinspection unchecked
                return criteriaBuilder.like((Expression<String>) findProperty(root, property), ((String) argument).replace("*", "%"));
            } else if(argument == null) {
                return criteriaBuilder.isNull(findProperty(root, property));
            } else {
                return criteriaBuilder.equal(findProperty(root, property),argument);
            }
        } else if (Objects.equals(operator, RSQLOperators.NOT_EQUAL)) {
            if(argument instanceof String) {
                //noinspection unchecked
                return criteriaBuilder.notLike((Expression<String>) findProperty(root, property), ((String) argument).replace("*", "%"));
            } else if (argument == null) {
                return criteriaBuilder.isNotNull(findProperty(root, property));
            } else {
                return criteriaBuilder.notEqual(findProperty(root, property), argument);
            }
        } else if (Objects.equals(operator, RSQLOperators.GREATER_THAN)) {
            //noinspection unchecked
            return criteriaBuilder.greaterThan((Expression) findProperty(root, property), (Comparable)argument);
        } else if (Objects.equals(operator, RSQLOperators.GREATER_THAN_OR_EQUAL)) {
            //noinspection unchecked
            return criteriaBuilder.greaterThanOrEqualTo((Expression) findProperty(root, property), (Comparable)argument);
        } else if (Objects.equals(operator, RSQLOperators.LESS_THAN)) {
            //noinspection unchecked
            return criteriaBuilder.lessThan((Expression) findProperty(root, property), (Comparable)argument);
        } else if (Objects.equals(operator, RSQLOperators.LESS_THAN_OR_EQUAL)) {
            //noinspection unchecked
            return criteriaBuilder.lessThanOrEqualTo((Expression) findProperty(root, property), (Comparable)argument);
        } else if (Objects.equals(operator, RSQLOperators.IN)) {
            return findProperty(root, property).in(args);
        } else if (Objects.equals(operator, RSQLOperators.NOT_IN)) {
            return criteriaBuilder.not(findProperty(root, property).in(args));
        } else {
            throw new UnsupportedOperationException("RSQL 不支持操作运算符: " + operator);
        }
    }

    /**
     * 处理嵌套属性
     * @param root
     * @param property
     * @return
     */
    protected Path<T> findProperty(Path<T> root, String property) {
        for(String name : property.split("\\.")) {
            root = root.get(name);
        }
        return root;
    }

    /**
     * 将查寻参数的值按照 entity 属性的类型 转化成恰当的类型
     * @param root
     * @return
     */
    protected List<Object> castArguments(Root<T> root) {
        List<Object> result = new ArrayList<>();
        Class<?> javaType = findProperty(root, property).getJavaType();
        arguments.forEach(arg -> {
            if(Objects.equals(javaType, Integer.class) || Objects.equals(javaType, int.class)) {
                result.add(Integer.valueOf(arg));
            } else if (Objects.equals(javaType, Long.class) || Objects.equals(javaType, long.class)) {
                result.add(Long.valueOf(arg));
            } else if (Objects.equals(javaType, Double.class) || Objects.equals(javaType, double.class)) {
                result.add(Double.valueOf(arg));
            } else if (Objects.equals(javaType, Float.class) || Objects.equals(javaType, float.class)) {
                result.add(Float.valueOf(arg));
            } else if (Objects.equals(javaType, BigDecimal.class)) {
                result.add(new BigDecimal(arg));
            } else if (Objects.equals(javaType, Boolean.class) || Objects.equals(javaType, boolean.class)) {
                result.add(Boolean.valueOf(arg));
            } else if (Objects.equals(javaType, LocalDate.class)) {
                result.add(LocalDate.parse(arg, DateTimeFormatter.ISO_DATE));
            } else if (Objects.equals(javaType, LocalDateTime.class)) {
                //兼容日期格式 与 日期时间格式
                if(arg.length() < "YYYY-MM-DDT".length()) {
                    result.add(LocalDate.parse(arg, DateTimeFormatter.ISO_DATE).atStartOfDay());
                } else {
                    result.add(LocalDateTime.parse(arg, DateTimeFormatter.ISO_DATE_TIME));
                }
            } else if (javaType.isEnum()) {
                for(Object e : javaType.getEnumConstants()) {
                    if (Objects.equals(arg, enumNameHelp((Enum<?>)e))) {
                        result.add(e);
                        return;
                    }
                }
                throw new IllegalArgumentException(arg + "RSQL 无法解析枚举类型: " + javaType);
            } else if (Objects.equals(javaType, String.class)) {
                result.add(arg);
            } else {
                throw new UnsupportedOperationException("当前 RSQL 不支持 java 数据类型" + javaType);
            }
        });
        return result;
    }

    private <E extends Enum<E> > String enumNameHelp(Enum<E> e) {
        return e.name();
    }
}
