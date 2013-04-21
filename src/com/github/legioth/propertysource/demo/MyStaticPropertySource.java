package com.github.legioth.propertysource.demo;

import java.util.List;

import com.github.legioth.propertysource.client.PropertySource;
import com.github.legioth.propertysource.client.annotations.BooleanConversion;
import com.github.legioth.propertysource.client.annotations.Namespace;
import com.github.legioth.propertysource.client.annotations.Property;

@Namespace("foo")
public interface MyStaticPropertySource extends PropertySource {
    public boolean bool();

    public String string();

    public String singleConf();

    public List<String> multiConf();

    @Property("foo.string")
    @BooleanConversion(truePattern = ".*[1,3]")
    public boolean isString1or3();

    @Property("user.agent")
    public String userAgent();

    @Property("user.agent")
    @BooleanConversion(trueValues = { "gecko1_8", "safari" })
    public boolean isOpenSourceEngine();

    @Property("user.agent")
    @BooleanConversion(truePattern = "ie\\d+")
    public boolean isIE();

    @Property("foo.singleConf")
    public List<String> singleAsList();

    @Property("foo.multiConf")
    @BooleanConversion(trueValues = { "Conf1", "Conf2" })
    public boolean multiContains1or2();

    @Property("foo.multiConf")
    @BooleanConversion(trueValues = { "Conf1", "Conf2" }, matchAll = true)
    public boolean multiContains1and2();
}
