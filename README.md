# Daggie Android Architecture Library

This library defines a lightweight modular architecture by leveraging [Dagger 2][dagger2] set bindings.

## Usage

1. Include Daggie and any desired addon modules in your Gradle dependencies:

```groovy
def daggieVersion = '1.0.1'
implementation "com.nextfaze.daggie:daggie:$daggieVersion"
implementation "com.nextfaze.daggie:daggie-okhttp:$daggieVersion"
implementation "com.nextfaze.daggie:daggie-slf4j:$daggieVersion"
implementation "com.nextfaze.daggie:daggie-logback:$daggieVersion"
implementation "com.nextfaze.daggie:daggie-rxjava:$daggieVersion"
implementation "com.nextfaze.daggie:daggie-foreground:$daggieVersion"
implementation "com.nextfaze.daggie:daggie-threeten:$daggieVersion"
implementation "com.nextfaze.daggie:daggie-glide:$daggieVersion"
implementation "com.nextfaze.daggie:daggie-moshi:$daggieVersion"
implementation "com.nextfaze.daggie:daggie-manup:$daggieVersion"
debugImplementation "com.nextfaze.daggie:daggie-leakcanary:$daggieVersion"
debugImplementation "com.nextfaze.daggie:daggie-stetho:$daggieVersion"
debugImplementation "com.nextfaze.daggie:daggie-devproxy:$daggieVersion"
```

2. Copy the Dagger 2 scaffolding from the sample project
3. Customize your app by editing `App.kt`

## Development Guidelines

This section contains some development guidelines.

#### Entry point is the module class

The entry point to each module is the `@Module` class. This should have kdoc with the following details:
* What bindings it provides
* What set bindings a user can provide, and what they do/how they interact
* Links to other interesting classes

#### Only expose bare minimum public APIs necessary

Use `internal` visibility to maximum degree possible, including module provider methods as they are not intended to be 
invoked directly

 [dagger2]: https://google.github.io/dagger/