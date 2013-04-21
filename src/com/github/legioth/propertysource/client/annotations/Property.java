package com.github.legioth.propertysource.client.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import com.github.legioth.propertysource.client.PropertySource;

/**
 * Defines the property to use by methods in an interface extending
 * {@link PropertySource}. When this annotation is on a method, that method will
 * return a value based on the defined GWT property. When this annotation is on
 * an interface, all methods in that interface will return a value based on the
 * defined GWT property. If this annotation is on both on the type and the
 * method, the value from the method is used.
 * <p>
 * If this annotation is not used, the property name will be determined using
 * the method name and any @{@link Namespace} annotation on the interface.
 * 
 * @see PropertySource
 * @see Namespace
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Documented
public @interface Property {
    /**
     * The name of the property.
     * 
     * @return the name of the property
     */
    public String value();
}
