Change Log
==========

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
