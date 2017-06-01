package com.nextfaze.daggie

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Process
import dagger.BindsInstance
import dagger.Component
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(InitializerModule::class, AppModule::class))
interface ApplicationComponent : AppMembers, Injector {
    fun initializer(): Initializer<Application>

    @Component.Builder
    interface Builder {
        @BindsInstance fun application(application: Application): Builder
        fun build(): ApplicationComponent
    }
}

@Module internal class InitializerModule {
    @Provides @ElementsIntoSet fun defaultInitializers() = emptySet<Initializer<Application>>()
    @Provides @ElementsIntoSet @Early fun defaultEarlyInitializers() = emptySet<Initializer<Application>>()
    @Provides @ElementsIntoSet fun defaultActivityLifecycleCallbacks() = emptySet<ActivityLifecycleCallbacks>()

    @Provides internal fun initializer(
            @Early earlyInitializers: Lazy<Set<Initializer<Application>>>,
            initializers: Lazy<Set<Initializer<Application>>>,
            activityLifecycleCallbacks: Lazy<Set<ActivityLifecycleCallbacks>>
    ): Initializer<Application> = { application ->
        earlyInitializers.get().forEach { it(application) }
        initializers.get().forEach { it(application) }
        activityLifecycleCallbacks.get().forEach { application.registerActivityLifecycleCallbacks(it) }
    }
}

open class DaggerApplication : Application() {
    /** The main application component. May throw if called from other than the [main process][isMainProcess]. */
    open val applicationComponent: ApplicationComponent by lazy {
        // Lazily initialized, so that ContentProviders can access the component earlier than onCreate().
        check(isMainProcess) { "Cannot instantiate application component on anything other than the main process" }
        createComponent().apply { initializer()(this@DaggerApplication) }
    }

    /** The [RunningAppProcessInfo] of the current process. */
    protected val currentProcessInfo: RunningAppProcessInfo by lazy {
        val pid = Process.myPid()
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.runningAppProcesses.orEmpty().first { it.pid == pid }
    }

    /** Indicates if this process is the main process. */
    protected val isMainProcess by lazy { !currentProcessInfo.processName.contains(":") }

    /**
     * Create the application component. Subclasses, as needed for tests for example, can override this to supply an
     * [ApplicationComponent] subclass.
     * @return An application component.
     */
    protected open fun createComponent(): ApplicationComponent =
            DaggerApplicationComponent.builder().application(this).build()
}

/** Returns the [ApplicationComponent] for the running app. */
val Context.applicationComponent: ApplicationComponent
    get() = (applicationContext as DaggerApplication).applicationComponent

/** Returns the member [Injector] fpr the running app. */
val Context.injector: Injector get() = applicationComponent