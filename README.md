# Cream Soda
Lightweight dependency injection for Java. 

[![License](http://img.shields.io/badge/license-APLv2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

#### About Cream Soda
[Cream Soda](https://github.com/canarin-solutions/cream-soda) is an ultra-lightweight dependency injection library for Java. Dependency injection frameworks are often perceived as "magical" and complex. 
CreamSoda - with just a few hundred lines of code - is probably the easiest, tiniest, most obvious one, 
and is quite efficient too (see comparison section below).
```xml
<dependency>
    <groupId>solutions.canarin</groupId>
    <artifactId>cream-soda-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

##### Usage - code examples
###### Create CreamSoda (the injector)
```java
CreamSoda creamSoda = CreamSoda.start();
```
An application typically needs a single CreamSoda instance.

###### Instantiating dependencies
Dependencies with @Inject constructor or a default constructor can be injected by CreamSoda without the need for
any configuration. Eg:
```java
public class A {
    @Inject
    public A(B b) {
        // ...
    }
}

public class B {
    @Inject
    public B(C c, D d) {
        // ...
    }
}

public class C {}

@Singleton
public class D {
    // something expensive or other reasons for being singleton
}
```
Creating an instance of A:
```java
A a = CreamSoda.instance(A.class);
```
