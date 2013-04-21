package com.github.legioth.propertysource.client;

import com.github.legioth.propertysource.client.annotations.BooleanConversion;
import com.github.legioth.propertysource.client.annotations.Namespace;
import com.github.legioth.propertysource.client.annotations.Property;

/**
 * A method in an interface extending this interface will be generated to return
 * the value of property defined in gwt.xml files.If no corresponding property
 * is defined, an error will be raised during GWT compilation.
 * <p>
 * By default, the name of the method defines the name of the property that it
 * should be bound to. In addition to this, a @{@link Namespace} annotation can
 * be added to the interface to add a prefix to all properties resolved based on
 * method names. The @{@link Property} annotation can be used to override the
 * default property selection.
 * <p>
 * A GWT property is either a <code>String</code> or a
 * <code>List&lt;String&gt</code> for multi value configuration properties.
 * <code>String</code> can also be used for properties that are defined as multi
 * value as long as only one value is selected. <code>List&lt;String&gt</code>
 * is also valid for properties that can only have one value. In addition to
 * this, a method returning <code>boolean</code> will return <code>true</code>
 * if the string value of the single value property is <code>"true"</code>,
 * <code>false</code> for the value <code>"false"</code> and fail to compile if
 * the property has some other value. Other ways of converting string property
 * values to a boolean can be defined using @{@link BooleanConversion}.
 * 
 * @see Property
 * @see Namespace
 * @see DynamicPropertySource
 */
public interface PropertySource {
    // Marker interface only
}
