package com.github.legioth.propertysource.client.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import com.github.legioth.propertysource.client.PropertySource;

/**
 * Defines the namespace used for property lookup based on method names in
 * {@link PropertySource} interfaces. The {@link #value()} of this annotation
 * will be used as a prefix when doing a property lookup based on a method name.
 * Methods for which a property name is defined using @{@link Property} do not
 * use the namespace.
 * <p>
 * If, for example, a property source interface marked with
 * <code>@Namespace("com.example")</code> contains a method named
 * <code>mode</code>, that method will return the value of the property
 * <code>com.example.mode</code>.
 */
@Target(ElementType.TYPE)
@Documented
public @interface Namespace {
    /**
     * The name of the namespace
     * 
     * @return the name of the namespace
     */
    public String value();
}
