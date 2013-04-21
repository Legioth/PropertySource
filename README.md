PropertySource
==============

A convenient way of compiling GWT property values into your module.

How
===

```xml
  <inherits name="com.github.legioth.propertysource.PropertySource" />

  <define-property values="true,false" name="foo.bool" />
  <set-property name="foo.bool" value="false" />

  <define-property values="string1,string2" name="foo.string" />
  <set-property name="foo.string" value="string1" />
```
```java
@Namespace("foo")
public interface MyStaticPropertySource extends PropertySource {
  public boolean bool();
  public String string();
}
```
```java
public class MyEntryPoint implements EntryPoint {
  @Override
  public void onModuleLoad() {
    MyStaticPropertySource staticSource = GWT.create(MyStaticPropertySource.class);
    if (staticSource.bool()) {
      Window.alert(staticSource.string());
    }
  }
}
```

Why
===

Replacing classes based on selection properties and controlling generator output using configuration properties is a powerful method for making sure the compiled javascript only contains what is strictly necessary. This way programming does however have a steep learning curve and might produce code that is not easily understood.

Consider a simple case where a simple tweak is needed in certain browsers but no in others. With classical deferred binding, the code would look something like this:

```
  public void doSomething() {
    doStuffCommonToAllBrowsers();
    doStuffNeededInOnlyOneBrowser();
    doMoreCommonStuff();
  }
  public doStuffNeededInOnlyOneBrowser() {
    // Nothing here, but subclasses might do something special
  }
```
To make  this work, you'd also need to create subclasses for each browser that requires special functionality, and for each subclass you'd also need a couple of lines of xml. Now consider the alternative:
```
  public void doSomething() {
    doStuffCommonToAllBrowsers();
    if (BrowserPermutation.isBrowser1()) {
      doStuffNeededInBrowser1();
    } else if (BrowserPermutation.isBrowser2()) {
      doStuffNeededInBrowser2();
    }
    doMoreCommonStuff();
  }
```
As long as the values of `BrowserPermutation.isBrowser1()` and `BrowserPermutation.isBrowser2()` are known at compile time, the GWT compiler will remove the browser specific code from the other permutations.

This approach can make application code simpler, but for framework level code it has the drawback of requiring changes the original code if support for a new browser is added.
