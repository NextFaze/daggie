package com.example.app

import dagger.Module

@Module(includes = arrayOf(ProductionModule::class, DebugModule::class))
class BuildTypeModule

interface BuildTypeInjector