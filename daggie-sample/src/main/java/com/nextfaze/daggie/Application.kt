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
@Component(modules = arrayOf(MainModule::class, BuildTypeModule::class))
interface ApplicationComponent : ApplicationInjector, ServiceInjector, ContentProviderInjector {
    fun retainedComponent(): RetainedComponent

    fun initializer(): Initializer<Application>
//    fun glideBuilderConfigurators(): Set<Configurator<GlideBuilder>>
//    fun glideConfigurators(): Set<Configurator<Glide>>
//    @Boot fun bootInitializer(): () -> Unit

    @Component.Builder
    interface Builder {
        @BindsInstance fun application(application: Application): Builder
        fun build(): ApplicationComponent
    }
}

/** Provides app initialization bindings. */
@Module class InitializerModule {
    @Provides @ElementsIntoSet fun defaultInitializers() = emptySet<Initializer<Application>>()
    @Provides @ElementsIntoSet @Early fun defaultEarlyInitializers() = emptySet<Initializer<Application>>()
    @Provides @ElementsIntoSet fun defaultActivityLifecycleCallbacks() = emptySet<ActivityLifecycleCallbacks>()

    @Provides fun initializer(@Early earlyInitializers: Lazy<Set<Initializer<Application>>>,
                              initializers: Lazy<Set<Initializer<Application>>>,
                              activityLifecycleCallbacks: Lazy<Set<ActivityLifecycleCallbacks>>):
            Initializer<Application> = { application ->
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

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Initialise multiple dex files as soon as possible
//        MultiDex.install(this)
    }

    /**
     * Create the application component. Subclasses, as needed for tests for example, can override this to supply an
     * [ApplicationComponent] subclass.
     * @return An application component.
     */
    protected open fun createComponent(): ApplicationComponent = DaggerApplicationComponent.builder()
            .application(this)
            .build()
}

val Context.applicationComponent: ApplicationComponent
    get() = (applicationContext as DaggerApplication).applicationComponent