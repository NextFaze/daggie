package com.nextfaze.daggie.autodispose

import com.uber.autodispose.ScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable

/** Convenience function to dismiss CheckResult warnings where we can't sufficiently suppress (i.e. init) */
fun <T : Any> Observable<T>.neverDispose() = autoDisposable(ScopeProvider.UNBOUND)
