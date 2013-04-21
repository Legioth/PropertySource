package com.github.legioth.propertysource.client.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import com.github.legioth.propertysource.client.PropertySource;

/**
 * Defines how string property values are converted to boolean return types for
 * methods in interfaces extending {@link PropertySource}. By default,
 * <code>true</code> is returned if the string value of the property is
 * <code>"true"</code> and <code>false</code> if it is <code>"false"</code>. A
 * compilation error is raised if the property has any other value.
 * <p>
 * If the property has multiple values, the method will return <code>true</code>
 * if any of the property values match the criteria. This behaviour can be
 * changed using {@link #matchAll()}.
 * <p>
 * This annotation has no effect on methods with return types other than
 * <code>boolean</code>.
 */
@Target(ElementType.METHOD)
@Documented
public @interface BooleanConversion {
    /**
     * Defines a regular expression that makes the method return
     * <code>true</code> when the property value matches and <code>false</code>
     * otherwise. The evaluation is done using
     * <code>propertyValue.matches(pattern)</code>.
     * 
     * @return a regular expression.
     */
    public String truePattern() default "";

    /**
     * Defines a list of values that makes the method return <code>true</code>
     * when the property value is found within the list and <code>false</code>
     * otherwise.
     * 
     * @return a regular expression.
     */
    public String[] trueValues() default {};

    /**
     * Defines whether all values of a multi value property must match the
     * criteria for the method to return <code>true</code>. The default is that
     * the method returns <code>true</code> as long as at least one of the
     * property values match.
     * <p>
     * This attribute has no effect for properties that do not have multiple
     * values.
     */
    public boolean matchAll() default false;
}
