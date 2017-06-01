package com.example.app

import dagger.Module

@Module(includes = arrayOf(ProductionModule::class, ReleaseModule::class))
class BuildTypeModule

interface BuildTypeInjector