package com.nextfaze.daggie.permissions.testing

import com.nextfaze.daggie.permissions.PermissionState
import com.nextfaze.daggie.permissions.Permissions
import dagger.Module
import dagger.Provides
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

@Module
class FakePermissionsModule {
    @Provides @Singleton
    internal fun permissions(fakePermissions: FakePermissions): Permissions = fakePermissions
}

@Singleton
class FakePermissions @Inject constructor() : Permissions {

    @Volatile var shouldShowRequestPermissionRationale: Observable<Set<String>> = Observable.create { it.onNext(emptySet()) }
    @Volatile var requestPermissions: Completable = Completable.complete()

    private val statesSubject = BehaviorSubject.createDefault(emptyMap<String, PermissionState>())

    var states: Map<String, PermissionState>
        get() = statesSubject.value!!
        set(value) = statesSubject.onNext(value)

    override fun permissionsGranted(permissions: Iterable<String>): Observable<Boolean> =
        Observable.combineLatest(permissions.map(::state)) { states ->
            states.all { state -> state == PermissionState.GRANTED }
        }

    override fun requestPermissions(permissions: Iterable<String>): Completable = Completable.defer { requestPermissions }

    override fun shouldShowRequestPermissionRationale(permission: String): Observable<Boolean> = Observable.defer {
        shouldShowRequestPermissionRationale.map { permission in it }.distinctUntilChanged()
    }

    override fun state(permission: String): Observable<PermissionState> = Observable.defer {
        statesSubject.map { it.getOrElse(permission) { PermissionState.DENIED } }
    }

    override fun notifyPermissionsChanged() {
        // No-op
    }
}
