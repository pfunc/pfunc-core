## PFunc

A FaaS approach to writing jenkins pipeline functions

### Aim

* make it easy to use pure java with any libraries you like (RxJava / RxJava2 / Spring / nothing) to write functions without version issues or any knowledge of jenkins or pipelines so they are easy to develop and test
* reuse them easily from inside a pipeline - or from a command line - or debug them
* support long running functions with wait loops
* capture the bootstrap from pipelines so we can easily re-run, test, debug functions in Che on a running pipeline workspace

### Sync v async

Support simple synchronous functions which return an optional value you can use in the pipeline.

```java
public class Main {
  /** Generate new version number */
  public String main(String someArgument) { 
     return "new version number here";
  }
}
```

then in the pipeline:
```groovy
def verison = pipelineFun("group:artifact:version").invoke("anArgument")
```

For async methods the function should return `Obserable<T>` from RxJava / RxJava2, `Future` from JDK or `Flux/Mono` from Spring

```java
/** lets wait until some event occurs... */

public Observable<String> longRunningThing() { 
   Subject<String> subject = someBackgroundThread(subject);
   // trigger long running background stuff
   
   return subject;
}
...
protected void someBackgroundThread(Subject subject) {
   subject.onNext(theAnswer);
   subject.onComplete();
}
```

then in the pipeline:
```groovy
def message = pipelineFun("group:artifact:version").invoke("anArgument")
echo "hello I got the message ${message}"
```

## Packaging

* define a Class with one (or more) functions inside
* wrap it in an executable jar
* add a META-INF/services file or something to point to the functions by name with the default one (maybe use an annotation to generate this all)

## ClassLoadering

* the pipeinefun jenkins pipeline plugin uses mvn repos to download the uberjar then uses a ClassLoader to load it (separate to the jenkins classloader to avoid clashes) then uses reflection to find the functions and their classes/methods
* do need to support frameworks like spring/CD etc to do DI?

## Function parameters

It might be nice to have framework specific injection of function parameters.

e.g. something like

```java
@PFunc
public Observable<String> myThingy(@Param("cheese") String cheese, @Param("foo") @NotNull Integer someCount) {
...
}
```

where the bootstrap code in the uber jar could do nice validation, introspection and type conversions etc.

### Bootstrap from the PFunc jenkins plugin

From the pipeline library perpsective it would be nice to have a really simple classloader agnostic approach that is unaware of type conversion or IoC magic; maybe a canonical class/method to invoke a function?

```java
// TODO this is generated/included via the uberjar
package io.pipelinefun.boostrap

public class BoostrapClass {
  public Object main(Map<String,Object> functionArguments, Map<String,Object> pipelineState, Map<String,String> env) {....}
}
```

Then folks could hand craft however they want to take those boostrap arguments and invoke some Java function using whatever IoC / DI frameworks, annotations or whatnot

## .pfunc.yml

We could have a '.pfunc.yml' file which we load from the local git workspace that could add the list of libraries and versions to load. There is then a canonical file to upgrade as we do CI/CD of pfunc libraries.

We can then inherit the global yaml file for the pfunc libraries, versions and any global or per pfunc config - then override the defaults on a per git repo basis.

    ---
    libraries:
      g:a:v
      foo:bar:1.2.3
    properties:
      foo: abc
    env:
      BAR: zzz
    pfuncs:
      # pfunc specific config
      myFunction:
        properties:
          foo: zzzz
        env:
          BAR: xxx


## Configuration

The default way to pass in configuration is via `System.getProperties()` and `System.getenv()` - as those are the most common ways to configure off the shelf Java code.

However to make it easy configure things explicitly in the pipeline we may need a jenkins step to help...

```groovy
// jenkinsfile...
def envVars = myPfunkLoadingConfig("foo")
def systemProperties = null
withPFunkConfig(envVars, systemProperties) {
  ...
  somePFunk(1234)
  anotherPFunk(456)
  
}
```

## TODO

* can we override System.setProperties() and System.setEnv() as we invoke a pfunk? Would be nice if we can

* should we be able to pass in a pipeline body to invoke? Something like

```groovy
// this is in the Jenkinsfile...

pipelineFun("group:artifact:version").invoke("anArgument") { result => 
   echo "Invoked pipeline block from inside the pipeline fun with value ${result}"
   sh "ls -al"
}
```