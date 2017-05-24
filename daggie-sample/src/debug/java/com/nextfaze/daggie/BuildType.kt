package com.nextfaze.daggie

import dagger.Module

@Module(includes = arrayOf(ProductionModule::class, DebugModule::class))
class BuildTypeModule

interface BuildTypeFragmentInjector

interface BuildTypeActivityInjector