package com.github.legioth.propertysource.demo;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.RootPanel;

public class DemoEntryPoint implements EntryPoint {

    @Override
    public void onModuleLoad() {
        MyDynamicPropertySource dynamicSource = GWT
                .create(MyDynamicPropertySource.class);
        MyStaticPropertySource staticSource = GWT
                .create(MyStaticPropertySource.class);

        String html = "<h1>Dynamic properties</h1>";
        html += "Combined: " + dynamicSource.combineProperties() + "<br />";
        html += "Is string 2: " + dynamicSource.isString2() + "<br />";
        html += "Multi conf reversed: " + dynamicSource.multiConfigReversed()
                + "<br />";
        html += "System properties: " + dynamicSource.readSystemProperties()
                + "<br />";

        html += "<h1>Static properties</h1>";
        html += "String: " + staticSource.string() + "<br />";
        html += "User agent: " + staticSource.userAgent() + "<br />";
        html += "Open source engine: " + staticSource.isOpenSourceEngine()
                + "<br />";
        html += "String1 or String3: " + staticSource.isString1or3() + "<br />";
        html += "bool: " + staticSource.bool() + "<br />";
        html += "Single conf: " + staticSource.singleConf() + "<br />";
        html += "Multi conf: " + staticSource.multiConf() + "<br />";
        html += "Single as list: " + staticSource.singleAsList() + "<br />";
        html += "Multi conf contains 1 or 2: "
                + staticSource.multiContains1or2() + "<br />";
        html += "Multi conf contains 1 and 2: "
                + staticSource.multiContains1and2() + "<br />";

        html += "<h1>Your browser...</h1>";
        html += "..." + getBrowserMessage() + "<br />";

        RootPanel.getBodyElement().setInnerHTML(html);
    }

    private static String getBrowserMessage() {
        if (UserAgentPermutation.isGecko()) {
            return "feels like a lizard";
        } else if (UserAgentPermutation.isSafari()) {
            return "is out on the savannah";
        } else if (UserAgentPermutation.isOpera()) {
            return "hits all the high notes";
        } else if (UserAgentPermutation.isIe()) {
            String message = "is made by Microsoft ";
            if (UserAgentPermutation.isIe6()) {
                message += "and is very old";
            } else if (UserAgentPermutation.isIe8()) {
                message += "and might be slightly dated";
            } else if (UserAgentPermutation.isIe9()) {
                message += " and is almost decent";
            } else {
                message += " and is newer than me";
            }
            return message;
        } else {
            // Unknown permutation
            return "doesn't like me";
        }
    }
}
