
-dontobfuscate
-dontwarn java.awt.**
-dontwarn java.nio.file.**
-dontwarn com.google.common.**
-dontwarn org.codehaus.mojo.animal_sniffer.**
-dontwarn java.lang.invoke.**
-dontwarn org.jetbrains.anko.internals.**

# Kotlin
-dontwarn kotlin.reflect.jvm.internal.**

# Fabric
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Retrofit
-dontwarn retrofit2.Platform$Java8
-dontwarn retrofit2.adapter.rxjava.CompletableHelper*

# PrettyTime
-keep class org.ocpsoft.prettytime.i18n.**

# Brightcove
-dontwarn com.brightcove.**
-keep public class com.brightcove.player.** {
    public *;
}
-keepclassmembers public class com.brightcove.player.** {
    public *;
}
-keepclasseswithmembers public class com.brightcove.player.** {
    public *;
}

# DevFun
-keep class com.nextfaze.devfun.**