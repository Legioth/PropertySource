package com.github.legioth.propertysource.demo;

import com.github.legioth.propertysource.client.PropertySource;
import com.github.legioth.propertysource.client.annotations.BooleanConversion;
import com.github.legioth.propertysource.client.annotations.Property;
import com.google.gwt.core.shared.GWT;

public class UserAgentPermutation {

    @Property("user.agent")
    interface Properties extends PropertySource {
        @BooleanConversion(truePattern = "gecko.*")
        public boolean isGecko();

        @BooleanConversion(trueValues = "safari")
        public boolean isSafari();

        @BooleanConversion(trueValues = "opera")
        public boolean isOpera();

        @BooleanConversion(truePattern = "ie\\d+")
        public boolean isIe();

        @BooleanConversion(trueValues = "ie6")
        public boolean isIe6();

        @BooleanConversion(trueValues = "ie8")
        public boolean isIe8();

        @BooleanConversion(trueValues = "ie9")
        public boolean isIe9();

        public String name();
    }

    private static Properties devModeCache;

    private static Properties get() {
        if (GWT.isProdMode()) {
            // GWT compiler inlines these, but it could inline if the null check
            // was here
            return GWT.create(Properties.class);
        } else {
            // Use same instance every time in devmode to avoid creating new
            // object every time
            if (devModeCache == null) {
                devModeCache = GWT.create(Properties.class);
            }
            return devModeCache;
        }
    }

    public static boolean isGecko() {
        return get().isGecko();
    }

    public static boolean isSafari() {
        return get().isSafari();
    }

    public static boolean isOpera() {
        return get().isOpera();
    }

    public static boolean isIe() {
        return get().isIe();
    }

    public static boolean isIe6() {
        return get().isIe6();
    }

    public static boolean isIe8() {
        return get().isIe8();
    }

    public static boolean isIe9() {
        return get().isIe9();
    }

    public static String getName() {
        return get().name();
    }
}
