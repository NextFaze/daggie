package com.nextfaze.daggie

//
// Application Scope
//

interface ApplicationInjector

interface ServiceInjector {
}

interface ContentProviderInjector

//
// Retained Scope
//

interface FragmentInjector : BuildTypeFragmentInjector {
}

interface DialogFragmentInjector

interface ActionProviderInjector

//
// Activity Scope
//

interface ActivityInjector : BuildTypeActivityInjector {
}

interface DialogInjector

interface ViewInjector {
}