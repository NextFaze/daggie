# Daggie Android Architecture Library

This library defines a lightweight modular architecture by leveraging [Dagger 2][dagger2] set bindings.

## Usage

1. Include Daggie and any desired addon modules in your Gradle dependencies:

    def daggieVersion = '1.0.0'
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

2. Copy the Dagger 2 scaffolding from the sample project
3. Customize your app by editing `App.kt`

 [dagger2]: https://google.github.io/dagger/