package com.github.legioth.propertysource.rebind;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.legioth.propertysource.client.DynamicPropertySource;
import com.github.legioth.propertysource.client.annotations.BooleanConversion;
import com.github.legioth.propertysource.client.annotations.Namespace;
import com.github.legioth.propertysource.client.annotations.Property;
import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.ConfigurationProperty;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.SelectionProperty;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.rebind.StringSourceWriter;

public class PropertySourceGenerator extends Generator {

    private interface TypeHandler<T> {
        public T getStaticReturnValue(TreeLogger logger,
                List<String> propertyValue, JMethod method)
                throws UnableToCompleteException;

        public void writeValue(TreeLogger logger, SourceWriter writer, T value);
    }

    private static final Map<String, TypeHandler<?>> typeHandlers = new HashMap<String, TypeHandler<?>>();
    static {
        typeHandlers.put(String.class.getName(), new TypeHandler<String>() {
            @Override
            public String getStaticReturnValue(TreeLogger logger,
                    List<String> propertyValue, JMethod method)
                    throws UnableToCompleteException {
                if (propertyValue.size() != 1) {
                    logger.log(Type.ERROR,
                            "String only supported for properties with only one value");
                    throw new UnableToCompleteException();
                }
                return propertyValue.get(0);
            }

            @Override
            public void writeValue(TreeLogger logger, SourceWriter writer,
                    String value) {
                writer.print("\"");
                writer.print(escape(value));
                writer.print("\"");
            }
        });
        typeHandlers.put("boolean", new TypeHandler<Boolean>() {
            @Override
            public Boolean getStaticReturnValue(TreeLogger logger,
                    List<String> propertyValueList, JMethod method)
                    throws UnableToCompleteException {
                String truePattern = "";
                String[] trueValues = new String[0];
                boolean matchAll = false;

                BooleanConversion booleanConversion = method
                        .getAnnotation(BooleanConversion.class);
                if (booleanConversion != null) {
                    truePattern = booleanConversion.truePattern();
                    trueValues = booleanConversion.trueValues();
                    matchAll = booleanConversion.matchAll();
                }

                for (String propertyValue : propertyValueList) {
                    boolean match = true;
                    boolean useDefaultLogic = true;
                    if (!truePattern.isEmpty()) {
                        match &= valueMatches(propertyValue, truePattern);
                        useDefaultLogic = false;
                    }
                    if (trueValues.length != 0) {
                        match &= isTrueValue(propertyValue, trueValues);
                        useDefaultLogic = false;
                    }

                    if (useDefaultLogic) {
                        match &= isTrueValue(logger, propertyValue);
                    }

                    if (matchAll && !match) {
                        // Found one that didn't match
                        return Boolean.FALSE;
                    } else if (match && !matchAll) {
                        // Found one that did match
                        return Boolean.TRUE;
                    }
                }

                // Found all without terminating
                if (matchAll) {
                    // This means that there wasn't any problem
                    return Boolean.TRUE;
                } else {
                    // This means no good match was found
                    return Boolean.FALSE;
                }
            }

            @Override
            public void writeValue(TreeLogger logger, SourceWriter writer,
                    Boolean value) {
                writer.print(value.toString());
            }
        });
        typeHandlers.put(List.class.getName(), new TypeHandler<List<String>>() {
            @Override
            public List<String> getStaticReturnValue(TreeLogger logger,
                    List<String> propertyValue, JMethod method)
                    throws UnableToCompleteException {
                JType type = method.getReturnType();
                JParameterizedType parameterizedType = type.isParameterized();
                if (parameterizedType == null
                        || !parameterizedType.getTypeArgs()[0]
                                .getQualifiedSourceName().equals(
                                        String.class.getName())) {
                    logger.log(
                            Type.ERROR,
                            type.getParameterizedQualifiedSourceName()
                                    + " is not supported. List<String> is the only supported List type.");
                    throw new UnableToCompleteException();
                }

                return propertyValue;
            }

            @Override
            public void writeValue(TreeLogger logger, SourceWriter writer,
                    List<String> value) {
                // Arrays.asList(value1, value2);
                writer.print(Arrays.class.getName());
                writer.print(".asList(");
                for (int i = 0; i < value.size(); i++) {
                    if (i != 0) {
                        writer.print(",");
                    }
                    writer.print("\"");
                    writer.print(escape(value.get(i)));
                    writer.print("\"");
                }
                writer.print(")");
            }
        });
    }

    @Override
    public String generate(TreeLogger logger, GeneratorContext context,
            String typeName) throws UnableToCompleteException {

        TypeOracle typeOracle = context.getTypeOracle();
        JClassType type = typeOracle.findType(typeName);

        if (type.isAbstract() && type.isInterface() == null) {
            logger.log(Type.ERROR,
                    "Target type should either be an interface or a non-abstract class");
            throw new UnableToCompleteException();
        }

        /*
         * Class name depends on the used properties -> must evaluate the type
         * before we know whether to generate a new class. Most of the effort
         * does currently go to the evaluation, so we might as well keep the
         * design simple by directly producing the class body at the same time
         * instead of first populating a data model and then using that data
         * model to produce the source only when needed.
         */
        StringSourceWriter sourceWriter = new StringSourceWriter();
        Set<String> usedSelectionProperties = writeMethods(
                logger.branch(Type.DEBUG, "Processing methods in " + typeName),
                context, type, sourceWriter);

        // Properties in deterministic order
        ArrayList<String> sortedProperties = new ArrayList<String>(
                usedSelectionProperties);
        Collections.sort(sortedProperties);

        StringBuilder prefixes = new StringBuilder("Impl");
        for (String propertyName : sortedProperties) {
            try {
                SelectionProperty property = context.getPropertyOracle()
                        .getSelectionProperty(logger, propertyName);
                if (property.getPossibleValues().size() > 1) {
                    /*
                     * Separating with not 100% safe as a_b + c and a + b_c
                     * would both give a_b_c, but the collision risk is quite
                     * minimal. AbstractClientBundleGenerator seems to use the
                     * same strategy in generateSimpleSourceName.
                     */
                    prefixes.append('_').append(property.getCurrentValue());
                }
            } catch (BadPropertyValueException e) {
                logger.log(Type.ERROR, "Could not recheck property", e);
                throw new UnableToCompleteException();
            }
        }

        String packageName = type.getPackage().getName();
        String className = type.getSimpleSourceName() + prefixes.toString();

        PrintWriter writer = context.tryCreate(logger, packageName, className);

        if (writer != null) {
            ClassSourceFileComposerFactory factory = new ClassSourceFileComposerFactory(
                    packageName, className);
            logger.log(Type.DEBUG,
                    "Assembling " + factory.getCreatedClassName());
            if (type.isInterface() != null) {
                factory.addImplementedInterface(type.getQualifiedSourceName());
            } else {
                factory.setSuperclass(type.getQualifiedSourceName());
            }

            SourceWriter realSourceWriter = factory.createSourceWriter(context,
                    writer);

            realSourceWriter.print(sourceWriter.toString());

            realSourceWriter.commit(logger);
        }

        String createdClassName = packageName + "." + className;
        return createdClassName;
    }

    private static Set<String> writeMethods(TreeLogger logger,
            GeneratorContext context, JClassType type, SourceWriter writer)
            throws UnableToCompleteException {
        Set<String> allUsedSelectionProperties = new HashSet<String>();

        JMethod[] methods = type.getMethods();
        for (JMethod method : methods) {
            if (method.isStatic()) {
                logger.log(Type.DEBUG,
                        "Ignoring static method " + method.getName());
                continue;
            }

            if (!method.isPublic()) {
                logger.log(Type.DEBUG,
                        "Ignoring non-public method " + method.getName());
                continue;
            }

            Set<String> usedSeletionProperties = writeMethod(
                    logger.branch(
                            Type.DEBUG,
                            "Processing method "
                                    + method.getReadableDeclaration()),
                    context, method, writer);
            if (usedSeletionProperties != null) {
                allUsedSelectionProperties.addAll(usedSeletionProperties);
            }
        }

        return allUsedSelectionProperties;
    }

    private static Set<String> writeMethod(TreeLogger logger,
            GeneratorContext context, JMethod method, SourceWriter writer)
            throws UnableToCompleteException {

        if (method.getParameters().length != 0) {
            logger.log(Type.ERROR, "Only supporting methods with no arguments");
            throw new UnableToCompleteException();
        }

        JType returnType = method.getReturnType();
        TypeHandler<Object> typeHandler = (TypeHandler<Object>) typeHandlers
                .get(returnType.getQualifiedSourceName());
        if (typeHandler == null) {
            logger.log(Type.ERROR, returnType.getQualifiedSourceName()
                    + " is not supported");
            throw new UnableToCompleteException();
        }

        final Set<String> usedSelectionProperties;
        Object returnValue;
        if (method.isAbstract()) {
            String propertyName = getPropertyName(method);
            String selectionPropertyValue = getStaticSelectionPropertyValue(
                    logger, context, propertyName);
            List<String> configurationPropertyValues = getStaticConfigurationPropertyValues(
                    context, propertyName);
            if (selectionPropertyValue == null
                    && configurationPropertyValues == null) {
                logger.log(Type.ERROR, "Property " + propertyName
                        + " not found");
                throw new UnableToCompleteException();
            }

            if (selectionPropertyValue != null) {
                returnValue = typeHandler.getStaticReturnValue(logger,
                        Collections.singletonList(selectionPropertyValue),
                        method);
                usedSelectionProperties = Collections.singleton(propertyName);
            } else {
                returnValue = typeHandler.getStaticReturnValue(logger,
                        configurationPropertyValues, method);
                usedSelectionProperties = null;
            }
        } else {
            usedSelectionProperties = new HashSet<String>();
            returnValue = getDynamicPropertyValue(
                    logger.branch(Type.DEBUG, "Evaluating method"), context,
                    method, usedSelectionProperties);
        }

        writer.println("%s {",
                method.getReadableDeclaration(false, false, false, false, true));
        writer.indent();

        writer.print("return ");
        typeHandler.writeValue(logger, writer, returnValue);
        writer.println(";");

        writer.outdent();
        writer.println("}");
        writer.println();

        return usedSelectionProperties;
    }

    private static Object getDynamicPropertyValue(TreeLogger logger,
            GeneratorContext context, JMethod method,
            Set<String> usedSelectionProperties)
            throws UnableToCompleteException {
        JClassType enclosingType = method.getEnclosingType();
        if (enclosingType.isAbstract()) {
            logger.log(Type.ERROR, "Can not evaluate method in abstract class");
            throw new UnableToCompleteException();
        }

        try {
            // TODO Load class using a new class loader to pick up changes
            Class<? extends DynamicPropertySource> targetClass = Class.forName(
                    enclosingType.getQualifiedSourceName()).asSubclass(
                    DynamicPropertySource.class);
            DynamicPropertySource source = targetClass.newInstance();

            Field proxyField = DynamicPropertySource.class
                    .getDeclaredField("proxy");
            proxyField.setAccessible(true);

            PropertyProxyImpl proxy = new PropertyProxyImpl(logger,
                    context.getPropertyOracle(), usedSelectionProperties);
            proxyField.set(source, proxy);

            Method instanceMethod = targetClass.getMethod(method.getName());
            Object result = instanceMethod.invoke(source);
            return result;

        } catch (Exception e) {
            logger.log(Type.ERROR, "Could not get dynamic type", e);
            throw new UnableToCompleteException();
        }
    }

    private static boolean valueMatches(String propertyValue, String pattern) {
        return propertyValue.matches(pattern);
    }

    private static boolean isTrueValue(String propertyValue, String[] trueValues) {
        for (String trueValue : trueValues) {
            if (trueValue.equals(propertyValue)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTrueValue(TreeLogger logger, String propertyValue)
            throws UnableToCompleteException {
        if (propertyValue.toLowerCase().equals("true")
                || propertyValue.equals("1")) {
            return true;
        } else if (propertyValue.toLowerCase().equals("false")
                || propertyValue.equals("0")) {
            return false;
        } else {
            logger.log(Type.ERROR, "Can not interpret " + propertyValue
                    + " as a boolean");
            throw new UnableToCompleteException();
        }
    }

    private static String getStaticSelectionPropertyValue(TreeLogger logger,
            GeneratorContext context, String propertyName) {
        try {
            SelectionProperty property = context.getPropertyOracle()
                    .getSelectionProperty(logger, propertyName);
            String propertyValue = property.getCurrentValue();
            return propertyValue;
        } catch (BadPropertyValueException e) {
            return null;
        }
    }

    private static List<String> getStaticConfigurationPropertyValues(
            GeneratorContext context, String propertyName) {
        try {
            ConfigurationProperty property = context.getPropertyOracle()
                    .getConfigurationProperty(propertyName);
            return property.getValues();
        } catch (BadPropertyValueException e) {
            return null;
        }
    }

    private static String getPropertyName(JMethod method) {
        // Use @Property on method if defined
        Property methodPropertyAnnotation = method
                .getAnnotation(Property.class);
        if (methodPropertyAnnotation != null) {
            return methodPropertyAnnotation.value();
        }

        JClassType enclosingType = method.getEnclosingType();

        // Use @Property on type if defined
        Property typePropertyAnnotation = enclosingType
                .getAnnotation(Property.class);
        if (typePropertyAnnotation != null) {
            return typePropertyAnnotation.value();
        }

        // Default to using method name
        String propertyName = method.getName();

        // Supplement with @Namespace on type if defined
        Namespace namespace = enclosingType.getAnnotation(Namespace.class);
        if (namespace != null) {
            return namespace.value() + "." + propertyName;
        } else {
            return propertyName;
        }
    }

}
