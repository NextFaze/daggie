Change Log
==========

## Version 5.2.0

_2018_02_20_

* Add support for proper cross-fading in `RemoteImageView`
* Fix bug where ManUp dialog buttons did not update in response to changes

## Version 5.1.0

_2018_02_14_

* Add ability to parse `Map`s into `Collections` using Moshi

## Version 5.0.0

_2018_02_09_

* Moshi key supplying functionality now requires `@RequiresKey` on models
* Add support for modern "platform unified" ManUp JSON format

## Version 4.0.1

_2018_02_02_

* ProGuard rules additions

## Version 4.0.0

_2018_02_02_

* Add support for maintenance mode in ManUp
* Add `daggie-permissions`, a reactive permissions module
* Change `daggie-logback` `LogbackAppender` typealias to `Appender<ILoggingEvent>`
* Add `Observable<Locale>` and `Observable<ZoneId>` (as well as `Flowable`) bindings to `daggie-threeten`
* Add support for parsing map keys into values to `daggie-moshi`, which is useful for Firebase Realtime Database 
  applications

## Version 3.1.0

_2018_01_15_

* Add `@MainThread` qualifying annotation, and a corresponding binding in `RxJava2SchedulerModule`

## Version 3.0.1

_2017_12_19_

* Fix layout preview error when using `RemoteImageView`

## Version 3.0.0

_2017_12_15_

* Migrated to Glide 4.x
    * This may be a breaking change if your app provides `Configurator<GlideBuilder>` set bindings. 
      If so, you'll need to implement an `AppGlideModule` by extending `com.nextfaze.daggie.glide.AppGlideModule`.
    * Apps that provided `Configurator<Glide>` set bindings must migrate to provide `Configurator<Registry>` bindings 
      instead. 
* `daggie-glide` now transitively exports `com.android.support:appcompat-v7`
* Added `RemoteImageView` to `daggie-glide`

## Version 2.0.1

_2017_12_06_

* ManUp now re-validates upon each request

## Version 2.0.0

_2017_09_26_

* Replace `daggie-rxjava` with `daggie-rxjava2`
* Replace dependencies on RxJava `1.x` with `2.x`

## Version 1.0.3

_2017_07_19_

* Fix ManUp retrying too rapidly after large number of attempts #3

## Version 1.0.2

_2017_07_10_

* Refine RxJava error hooks #2

## Version 1.0.1

_2017_06_26_

* Update to Kotlin 1.1.3

## Version 1.0.0

_2017-06-23_

* First release
