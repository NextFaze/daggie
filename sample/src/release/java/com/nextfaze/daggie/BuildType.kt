package com.nextfaze.daggie

import dagger.Module

@Module(includes = arrayOf(ProductionModule::class, ReleaseModule::class))
class BuildTypeModule

interface BuildTypeFragmentInjector

interface BuildTypeActivityInjector