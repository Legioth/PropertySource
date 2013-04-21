package com.github.legioth.propertysource.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.legioth.propertysource.client.DynamicPropertySource;

public class MyDynamicPropertySource extends DynamicPropertySource {

    public boolean isString2() {
        return "string2".equals(getSelectionPropertyValue("foo.string"));
    }

    public String combineProperties() {
        String value = getSelectionPropertyValue("user.agent");
        List<String> configurationPropertyValues = getConfigurationPropertyValues("foo.multiConf");

        StringBuilder sb = new StringBuilder();
        for (String string : configurationPropertyValues) {
            sb.append(value).append(string);
        }
        return sb.toString();
    }

    public List<String> multiConfigReversed() {
        List<String> propertyValues = getConfigurationPropertyValues("foo.multiConf");

        // Make a copy
        propertyValues = new ArrayList<String>(propertyValues);

        // Reverse
        Collections.reverse(propertyValues);

        return propertyValues;
    }

    public String readSystemProperties() {
        /*
         * Even though the code is never run by GWT, the compiler will still
         * check that it's valid. System.getProperty(String) is not supported by
         * GWT, but System.getProperty(String, String) is.
         */
        return "Module compiled with Java "
                + System.getProperty("java.version", "") + " from "
                + System.getProperty("java.vendor", "") + " on "
                + System.getProperty("os.arch", "") + " "
                + System.getProperty("os.name", "") + " "
                + System.getProperty("os.version", "");
    }
}
