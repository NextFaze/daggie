Change Log
==========

## Unreleased

No unreleased changes.

## Version 8.3.0

_2019-05-16_

* Fix bug where `Permissions.state()` never emitted a result unless an `Activity` started, making it unusable inside a
  service.

## Version 8.2.1

_2019-05-02_

* Update Dagger to `2.22.1`

## Version 8.2.0

_2019-03-04_

* Add `Single.onErrorComplete()` overload
* Deprecate `backoff()`
* Add `backoffRetry()` and `backoffRepeat()`

## Version 8.1.5

_2019-03-01_

* Fix typo in slf4j `AndroidManifest` package declaration
* Add RxJava `filter(Not)Empty()` overloads for `Single` and `Maybe`

## Version 8.1.4

_2019-02-18_

* Update Dagger to `2.21`
* Update ThreeTenABP, which includes an updated time zone database `2018g`

## Version 8.1.3

_2019-02-15_

* Fix dev proxy asking to install every time on API 28 and some devices

## Version 8.1.2

_2019-02-15_

* Fix dev proxy failing to install on API 28

## Version 8.1.1

_2019-01-09_

* Fix NPE thrown from `daggie-foreground` module
* Upgrade to Dagger `2.20`

## Version 8.1.0

_2018-11-28_

* Add `daggie-optional` module, which contains provides an `Optional<T>` class suitable for use with RxJava 2
* Add a variety of RxJava extensions and utilities to `daggie-rxjava2`
* Add `daggie-rxrelay` module, which contains factories, extensions, and property delegates for `Relay`s
* Add `daggie-rxpreferences` module, which contains extensions for use with `Optional<T>`
* Add `daggie-permissions-testing` module, which contains test implementations of classes in `daggie-permissions`
* Add `daggie-foreground-testing` module, which contains a test foreground manager
* Add Moshi `Moshi.adapter<T>()` reified type overload

## Version 8.0.0

_2018-11-13_

* Migrate to AndroidX
* Upgrade to Dagger `2.19`
* Support modern ManUp protocol (the one with shorter, more concise keys)

## Version 7.1.0

_2018-10-26_

* Add API for querying permission state in `daggie-permissions`

## Version 7.0.0

_2018-10-16_

* Add `daggie-autodispose` module, which provides an alternative to `autodispose-android-archcomponents` that implements
  a `Fragment` view scope
* Add `AndroidAdaptersModule` to `daggie-moshi`, which provides JSON adapters for commonly used Android types
* Eliminate delay from `daggie-foreground` implementation
* Stop exporting Gson, Retrofit, etc from `daggie-manup`
* Remove all deprecated `RemoteImageView` properties
* Remove deprecated `@Early` annotation

## Version 6.0.0

_2018-09-03_

* Enable RxAndroid's async mode
* Update to Dagger 2.17

## Version 5.2.4

_2018-08-13_

* Add support for arbitrary Glide models to `RemoteImageView`
* Improve exception when Glide module not configured properly

## Version 5.2.3

_2018-05-30_

* Don't install invalid proxy to `OkHttpClient.Builder`

## Version 5.2.2

_2018-03-07_

* Fix bug where Moshi key supplying included the key in serialized single objects

## Version 5.2.1

_2018-02-28_

* Don't initialize LeakCanary when app was built with JRebel

## Version 5.2.0

_2018-02-20_

* Add support for proper cross-fading in `RemoteImageView`
* Fix bug where ManUp dialog buttons did not update in response to changes

## Version 5.1.0

_2018-02-14_

* Add ability to parse `Map`s into `Collections` using Moshi

## Version 5.0.0

_2018-02-09_

* Moshi key supplying functionality now requires `@RequiresKey` on models
* Add support for modern "platform unified" ManUp JSON format

## Version 4.0.1

_2018-02-02_

* ProGuard rules additions

## Version 4.0.0

_2018-02-02_

* Add support for maintenance mode in ManUp
* Add `daggie-permissions`, a reactive permissions module
* Change `daggie-logback` `LogbackAppender` typealias to `Appender<ILoggingEvent>`
* Add `Observable<Locale>` and `Observable<ZoneId>` (as well as `Flowable`) bindings to `daggie-threeten`
* Add support for parsing map keys into values to `daggie-moshi`, which is useful for Firebase Realtime Database 
  applications

## Version 3.1.0

_2018-01-15_

* Add `@MainThread` qualifying annotation, and a corresponding binding in `RxJava2SchedulerModule`

## Version 3.0.1

_2017-12-19_

* Fix layout preview error when using `RemoteImageView`

## Version 3.0.0

_2017-12-15_

* Migrated to Glide 4.x
    * This may be a breaking change if your app provides `Configurator<GlideBuilder>` set bindings. 
      If so, you'll need to implement an `AppGlideModule` by extending `com.nextfaze.daggie.glide.AppGlideModule`.
    * Apps that provided `Configurator<Glide>` set bindings must migrate to provide `Configurator<Registry>` bindings 
      instead. 
* `daggie-glide` now transitively exports `com.android.support:appcompat-v7`
* Added `RemoteImageView` to `daggie-glide`

## Version 2.0.1

_2017-12-06_

* ManUp now re-validates upon each request

## Version 2.0.0

_2017-09-26_

* Replace `daggie-rxjava` with `daggie-rxjava2`
* Replace dependencies on RxJava `1.x` with `2.x`

## Version 1.0.3

_2017-07-19_

* Fix ManUp retrying too rapidly after large number of attempts #3

## Version 1.0.2

_2017-07-10_

* Refine RxJava error hooks #2

## Version 1.0.1

_2017-06-26_

* Update to Kotlin 1.1.3

## Version 1.0.0

_2017-06-23_

* First release
